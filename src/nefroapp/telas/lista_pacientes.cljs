(ns nefroapp.telas.lista-pacientes
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
    [material-list]
    [list-item :as material-list-item]
    [list-item-text :as material-list-item-text]
    [nefroapp.storage-module.core :as storage-module]
    [nefroapp.telas.shell-components :as shell]
    [nefroapp.util :as util :refer [<sub >evt]]
    [re-frame.core :as re-frame]
    [search-icon :as mui-icon-search]
    [clojure.string :as str]
    ))

(def farmacos-padrao
  ["Eritropoetina"
   "Hidróxido de Ferro"
   "Calcijex"
   "Sevelamer (Renagel)"
   "Calcitriol"
   "Cinacalcete"
   "Paricalcitol"])

(defn receita-padrao [farmacos-list]
  {:criada-em (util/today)
   :editada-em (util/today)
   :farmacos (zipmap farmacos-list (repeat {:prescricao ""}))})

(defn-traced novo-paciente
  [app-state]
  (let [paciente-nome (js/prompt "Qual o nome do paciente?")
        trimmed-nome (-> paciente-nome clojure.string/trim (clojure.string/replace #"\s\s+" " "))
        pacientes-keys (keys (get-in app-state [:domain :pacientes]))
        next-id (if (empty? pacientes-keys)
                  -1 ;; Se começar do zero o firebase vai resgatar o valor como array e não como map.
                  (dec (apply min pacientes-keys)))
        farmacos-list (get-in app-state [:domain :farmacos-padrao] farmacos-padrao)]
    (if (empty? trimmed-nome)
      app-state
      (-> app-state
          (assoc-in [:domain :pacientes next-id]
                    {:id next-id
                     :nome trimmed-nome
                     :receitas (list (receita-padrao farmacos-list))})
          (assoc-in [:ui :screen-state] "receita")
          (assoc-in [:ui :paciente-selecionado] next-id)
          (assoc-in [:ui :actions-menu :opened?] false)
          storage-module/save-or-restore-domain!))))
(re-frame/reg-event-db ::novo-paciente novo-paciente)

(defn-traced select-paciente
  [app-state [event selected-id]]
  (-> app-state
      (assoc-in [:ui :paciente-selecionado] selected-id)
      (assoc-in [:ui :screen-state] "receita")))
(re-frame/reg-event-db ::select-paciente select-paciente)

(defn replace-accents [s]
  (clojure.string/replace s #"[áâéêíóôãçõ]" {"á" "a"
                                             "â" "a"
                                             "é" "e"
                                             "ê" "e"
                                             "í" "i"
                                             "ó" "o"
                                             "ô" "o"
                                             "ã" "a"
                                             "ç" "c"
                                             "õ" "o"}))

(defn pacientes-e-receitas-data
  [app-state]
  (let [buscar-nome (get-in app-state [:ui :buscar-paciente] "")
        sanitize-fn (comp replace-accents str/lower-case str/trim)
        search-fn #(str/includes?
                     (sanitize-fn (:nome %))
                     (sanitize-fn buscar-nome))
        pacientes (-> app-state
                      (get-in [:domain :pacientes] {})
                      (vals)
                      (as-> ps (filter search-fn ps)))
        ultima-fn (fn [{:keys [receitas]}]
                    (->> receitas
                         (map :editada-em)
                         sort
                         last
                         util/yyyy-mm-dd->dd-mm-yyyy))]
    (map #(assoc % :ultima-receita-editada (ultima-fn %)) pacientes)))
(re-frame/reg-sub ::pacientes-e-receitas-data pacientes-e-receitas-data)

(defn-traced set-buscar-paciente
  [app-state [event s]]
  (assoc-in app-state [:ui :buscar-paciente] s))
(re-frame/reg-event-db ::set-buscar-paciente set-buscar-paciente)

(defn get-buscar-paciente
  [app-state]
  (get-in app-state [:ui :buscar-paciente] ""))
(re-frame/reg-sub ::get-buscar-paciente get-buscar-paciente)

(defn input-buscar []
  [:paper-input
   {:style {:width "100%"
            :marginTop "-18px"}
    :label "Buscar"
    :onInput (util/throttle-for-mutable-args
               500
               #(>evt [::set-buscar-paciente %])
               #(-> % .-target .-value)) }
   [:> mui-icon-search
    {:slot "suffix"}]])

(defn component []
  [:<>
   [input-buscar]
   [:> material-list
    (cond
      (and (empty? (<sub [::pacientes-e-receitas-data]))
           (empty? (<sub [::get-buscar-paciente])))
      [:p "Sem pacientes cadastrados ainda."] 

      (empty? (<sub [::pacientes-e-receitas-data]))
      [:p "Nenhum paciente com esse nome."])
    (for [{:keys [id nome ultima-receita-editada]}
          (<sub [::pacientes-e-receitas-data])]
      ^{:key id}
      [:> material-list-item
       {:button true
        :onClick #(>evt [::select-paciente id])}
       [:> material-list-item-text
        {:primary nome
         :secondary (str "Receita editada em: "ultima-receita-editada)}]])]])

(defn view []
  [shell/default
   [component]])

(defn loading []
  [shell/loading])
