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
    ))

(defn receita-padrao []
  {:criada-em (util/today)
   :editada-em (util/today)
   :farmacos {"Tempo" {:prescricao ""},
              "Fluxo de Sangue" {:prescricao ""}
              "Fluxo de Dialisato" {:prescricao ""}
              "Acesso Vascular" {:prescricao ""}
              "Capilar" {:prescricao ""}
              "Peso Seco" {:prescricao ""}
              "KCL" {:prescricao ""}
              "Ca" {:prescricao ""}
              "Glicose" {:prescricao ""}
              "Na" {:prescricao ""}
              "BIC" {:prescricao ""}
              "Temperatura" {:prescricao ""}
              "Heparina" {:prescricao ""}
              "UF Máxima" {:prescricao ""}}})

(defn-traced novo-paciente
  [app-state]
  (let [paciente-nome (js/prompt "Qual o nome do paciente?")
        trimmed-nome (-> paciente-nome clojure.string/trim (clojure.string/replace #"\s\s+" " "))
        pacientes-keys (keys (get-in app-state [:domain :pacientes]))
        next-id (if (empty? pacientes-keys)
                  -1 ;; Se começar do zero o firebase vai resgatar o valor como array e não como map.
                  (inc (apply max pacientes-keys)))]
    (if (empty? trimmed-nome)
      app-state
      (-> app-state
          (assoc-in [:domain :pacientes next-id]
                    {:id next-id
                     :nome trimmed-nome
                     :receitas (list (receita-padrao))})
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

(defn pacientes-e-receitas-data
  [app-state]
  (let [pacientes (vals (get-in app-state [:domain :pacientes] {}))
        ultima-fn (fn [{:keys [receitas]}]
                    (->> receitas
                         (map :editada-em)
                         sort
                         last
                         util/yyyy-mm-dd->dd-mm-yyyy))]
    (map #(assoc % :ultima-receita-editada (ultima-fn %)) pacientes)))
(re-frame/reg-sub ::pacientes-e-receitas-data pacientes-e-receitas-data)

(defn component []
  [:<>
   [:paper-input
    {:style {:width "100%"
             :marginTop "-18px"}
     :label "Buscar"
     ;; :onFocus #(>evt [::clear-errors])
     ;; :value (<sub [::email])
     ;; :onBlur #(>evt [::set-login-property :email (-> % .-target .-value)])
     :onClick #(js/alert "😑 Funcionalidade não disponível ainda.")}
    [:> mui-icon-search
     {:slot "suffix"}]]
   [:> material-list
    (when (empty? (<sub [::pacientes-e-receitas-data]))
      [:p "Sem pacientes cadastrados ainda."])
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
