(ns nefroapp.telas.receita
  (:require
    [button :as material-button]
    [chevron-left-icon :as mui-icon-chevron-left]
    [chevron-right-icon :as mui-icon-chevron-right]
    [icon-button :as material-icon-button]
    [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
    [nefroapp.domain.receita-historico :as receita-historico]
    [nefroapp.telas.shell-components :as shell]
    [nefroapp.util :as util :refer [<sub >evt]]
    [re-frame.core :as re-frame]
    [tick.alpha.api :as tick]
    ;; [List :as material-list]
    ;; [ListItem :as material-list-item]
    ;; [ListItemText :as material-list-item-text]
    ;; [SearchIcon :as mui-icon-search]

    [reagent.core :as reagent]
    ))

(defn-traced select-history-value-before
  [app-state [event farmaco-nome total]]
  (update-in app-state [:ui :receita :historico farmaco-nome]
             #(-> % inc (min total))))
(re-frame/reg-event-db ::select-history-value-before select-history-value-before)

(defn-traced select-history-value-after
  [app-state [event farmaco-nome]]
  (update-in app-state [:ui :receita :historico farmaco-nome]
             #(-> % dec (max 0))))
(re-frame/reg-event-db ::select-history-value-after select-history-value-after)

(defn receita-historico-selecionado
  [app-state]
  (get-in app-state [:ui :receita :historico] {}))
(re-frame/reg-sub
  ::receita-historico-selecionado
  receita-historico-selecionado)

(defn today [] "2020-04-23T13:13:23.826-03:00[SYSTEM]" #_(str (tick/zoned-date-time)))

(defn get-paciente-selecionado-receitas [app-state]
  (let [paciente-selecionado (get-in app-state [:ui :paciente-selecionado] 0)
        receitas (get-in app-state [:domain :pacientes paciente-selecionado :receitas])
        sorted-receitas (reverse (sort-by :criada-em receitas)) ;; TODO: Precisa ordenar? Avaliar.

        get-date #(second (when % (re-find #"(\d{4}\D\d{1,2}\D\d{1,2})" %)))
        tem-receita-de-hoje? (= (get-date (today))
                                (get-date (:criada-em (first sorted-receitas))))

        ;; Vou usar a receita de hoje para mostrar o valor de prescrição no input.
        todays-receita (when tem-receita-de-hoje? (first sorted-receitas))

        ;; A receita de hoje é a que está sendo editada, então não vou
        ;; considerá-la pra criar o histórico.
        sorted-receitas (if tem-receita-de-hoje?
                          (rest sorted-receitas)
                          sorted-receitas)]
    {:todays-receita todays-receita
     :sorted-receitas sorted-receitas}))

(defn set-todays-receita
  [app-state [event farmaco-nome new-value]]
  (let [{:keys [todays-receita sorted-receitas]} (get-paciente-selecionado-receitas app-state)
        farmacos-sem-prescricao (into {} (for [[farmaco-nome _] (:farmacos (first sorted-receitas))] {farmaco-nome {:prescricao ""}}))
        updated-todays-receita (if todays-receita
                                 (-> todays-receita
                                     (assoc :editada-em (today))
                                     (assoc-in [:farmacos farmaco-nome :prescricao] new-value))
                                 {:criada-em (today)
                                  :editada-em (today)
                                  :farmacos (assoc-in farmacos-sem-prescricao [farmaco-nome :prescricao] new-value)})
        paciente-selecionado (get-in app-state [:ui :paciente-selecionado] 0)]
    (-> app-state
        (assoc-in [:ui :receita :editando farmaco-nome] false)
        (assoc-in [:domain :pacientes paciente-selecionado :receitas]
                  (conj sorted-receitas
                        updated-todays-receita)))))
(re-frame/reg-event-db ::set-todays-receita set-todays-receita)

(defn farmacos-lista
  [app-state]
  (let [;; Vou usar a receita de hoje para mostrar o valor de prescrição no input.
        ;; A receita de hoje é a que está sendo editada, então não vou
        ;; considerá-la pra criar o histórico.
        {:keys [todays-receita sorted-receitas]} (get-paciente-selecionado-receitas app-state)

        ;; Cria algo do tipo
          ;; {"farmacoA" [{:data "2020-01-01" :value "10ml"}
          ;;              {:data "2020-02-01" :value "20ml"}]
          ;;  "farmacoB" [{:data "2020-01-01" :value "50ml"}
          ;;              {:data "2020-02-01" :value "70ml"}]}
        farmacos->date-values-map (fn [criada-em farmacos]
                                    (into {} (for [[nome {:keys [prescricao]}] farmacos]
                                               {nome [{:date criada-em :value prescricao}]})))

        farmacos-history (reduce (fn [acc {:keys [criada-em farmacos]}]
                                   (merge-with concat acc
                                               (farmacos->date-values-map criada-em farmacos)))
                                 {} sorted-receitas)]
    (into [] (for [[farmaco-nome historico] farmacos-history]
               {:nome farmaco-nome
                :historico (receita-historico/compact-history
                             (today)
                             historico)
                :prescricao-de-hoje (some-> todays-receita :farmacos
                                            (get farmaco-nome) :prescricao)}))))
(re-frame/reg-sub ::farmacos-lista farmacos-lista)

(def left {:display "flex"
           :marginBottom "7px"
           :position "absolute"
           :transform "translateX(-200%)"
           :clipPath "inset(0px 0px 0px 200%)"
           :transition "1s ease-out"})

(def middle {:display "flex"
             :marginBottom "7px"
             :transform "translateX(0px)"
             :position "absolute"
             :clipPath "inset(0px)"
             :transition "1s ease-in"})

(def right {:display "flex"
            :marginBottom "7px"
            :position "absolute"
            :transform "translateX(200%)"
            :clipPath "inset(0px 200% 0px 0px)"
            :transition "1s ease-out"}) ;; TODO: Animação ainda não está boa.

(defn titulo-farmaco [titulo]
  [:h3
   {:style {:marginBottom "5px"}}
   titulo])

(defn historico-farmaco [{:keys [historico nome]}]
  (let [idx-selecionado (or ((<sub [::receita-historico-selecionado]) nome) 0)
        last-idx (dec (count historico))
        idx-- (max (dec idx-selecionado) 0)
        idx++ (+ 2 idx-selecionado)
        _ (js/console.log "hhh" nome idx-- idx++)
        ]
    [:div
     {:style {:transform "translateY(-5px)"
              :position "relative"}}
     (for [idx (range idx-- idx++)]
       ^{:key idx}
       [:div
        (condp #(%1 %2 idx) idx-selecionado
            < {:style left}
            > {:style right}
            = {:style middle}
            {:style left})
        (when (< idx last-idx)
          [:> material-icon-button
           {:color "inherit"
            :onClick #(>evt [::select-history-value-before nome last-idx])}
           [:> mui-icon-chevron-left]])
        [:p {:style {:color "gray"}}
         (nth historico idx)]
        (when (> idx 0)
          [:> material-icon-button
           {:color "inherit"
            :onClick #(>evt [::select-history-value-after nome])}
           [:> mui-icon-chevron-right]])])
     [:p.placeholder-for-height-computation
      {:style {:padding "0px 48px"
               :visibility "hidden"
               ;; :transition "max-height 4s ease-in-out"
               ;; :overflow "hidden"
               ;; :height "auto"
               ;; :maxHeight "100%"
               ;; ;; :maxHeight "0px"
               }}
      (nth historico idx-selecionado)]]))

(defn input-farmaco [{:keys [value onBlur]}]
  [:div
   {:style {:border "1px solid #BDBDBD"
            :borderRadius "8px"
            :width "100%"
            :padding "0px 3px"}}
   [:paper-textarea
    {:style {:width "100%"
             :marginTop "-18px"}
     :placeholder "Prescrição de hoje"
     :value value
     :onBlur onBlur}]])

(defn repetir-farmaco-button [{:keys [onClick]}]
  [:> material-button
   {:style {:marginLeft "10px"}
    :onClick onClick
    :variant "contained"
    :size "small"}
   "Repetir"])

(defn input-line [{:keys [prescricao-de-hoje nome historico-selecionado]}]
  [:div
   {:style {:display "flex"
            :alignItems "center"}}
   [input-farmaco
    {:value prescricao-de-hoje
     :onBlur #(>evt [::set-todays-receita nome (-> % .-target .-value)])}]
   (when (empty? prescricao-de-hoje)
     [repetir-farmaco-button
      {:onClick #(>evt [::set-todays-receita nome historico-selecionado])}])])

(defn edit-farmaco
  [app-state [event farmaco-nome]]
  ;; TODO: Refatorar para colocar os farmacos num map que pode dar merge no
  ;; farmaco do domain.
  (assoc-in app-state [:ui :receita :editando farmaco-nome] true))
(re-frame/reg-event-db ::edit-farmaco edit-farmaco)

(defn prescricao-hoje-line [{:keys [prescricao-de-hoje nome]}]
  [:div
   {:style {:display "flex"
            :alignItems "center"}}
   [:div prescricao-de-hoje]
   [:> material-button
    {:style {:marginLeft "10px"}
     :onClick #(>evt [::edit-farmaco nome])
     :variant "contained"
     :size "small"}
    "Editar"]])

(defn editando?
  [app-state]
  ;; TODO: Refatorar quando puder dar merge no farmaco domain e ui
  (get-in app-state [:ui :receita :editando] {}))
(re-frame/reg-sub ::editando? editando?)

(defn farmaco-component [{{:keys [historico nome prescricao-de-hoje]} :farmaco}]
  (let [idx-selecionado (or ((<sub [::receita-historico-selecionado]) nome) 0)
        historico-selecionado (as-> historico $
                                  (nth $ idx-selecionado)
                                  (clojure.string/split $ #"- ")
                                  (rest $)
                                  (clojure.string/join "- " $))]
    [:<>
     [titulo-farmaco nome]
     ;; TODO: Colocar essa lógica do "and" num único reg-sub quando for
     ;; possível dar merge nos farmacos do domain e ui.
     (if (and prescricao-de-hoje (not ((<sub [::editando?]) nome)))
       [prescricao-hoje-line
        {:prescricao-de-hoje prescricao-de-hoje
         :nome nome}]
       [:<>
        [historico-farmaco
           {:historico historico
            :nome nome}]
        [input-line
           {:prescricao-de-hoje prescricao-de-hoje
            :nome nome
            :historico-selecionado historico-selecionado}]])]))

(defn component []
  [:div
   {:style {:overflowX "hidden"}}
   (when (empty? (<sub [::farmacos-lista]))
     [:h3 "Nenhum medicamento cadastrado ainda."])
   (for [farmaco (<sub [::farmacos-lista])] ;; TODO: Dar merge com informações da UI
     ^{:key (:nome farmaco)}
     [farmaco-component
      {:farmaco farmaco}])])

(defn view []
  [shell/default
   [shell/error-boundary
    {:if-error [:h1 "Erro receita"]} ;; TODO: replace error-view
    [component]]])
