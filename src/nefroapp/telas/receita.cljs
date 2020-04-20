(ns nefroapp.telas.receita
  (:require
    [Button :as material-button]
    [ChevronLeftIcon :as mui-icon-chevron-left]
    [ChevronRightIcon :as mui-icon-chevron-right]
    [IconButton :as material-icon-button]
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

        get-date #(second (re-find #"(\d{4}\D\d{1,2}\D\d{1,2})" %))
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
    (assoc-in app-state
              [:domain :pacientes paciente-selecionado :receitas]
              (conj sorted-receitas
                    updated-todays-receita))))
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
           :transition "1s ease"})

(def middle {:display "flex"
             :marginBottom "7px"
             :transform "translateX(0px)"
             :position "absolute"
             :clipPath "inset(0px)"
             :transition "1s ease"})

(def right {:display "flex"
            :marginBottom "7px"
            :position "absolute"
            :transform "translateX(200%)"
            :clipPath "inset(0px 200% 0px 0px)"
            :transition "1s ease"})

(defn titulo-farmaco [titulo]
  [:h3
   {:style {:marginBottom "5px"}}
   titulo])

(defn historico-farmaco [{:keys [historico nome]}]
  (let [idx-selecionado (or ((<sub [::receita-historico-selecionado]) nome) 0)
        last-idx (dec (count historico))
        idx-- (max (dec idx-selecionado) 0)
        idx++ (inc idx-selecionado)]
    [:<>
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
     :value value
     :onBlur onBlur}]])

(defn repetir-farmaco-button [{:keys [onClick]}]
  [:> material-button
   {:style {:marginLeft "10px"}
    :onClick onClick
    :variant "contained"
    :size "small"}
   "Repetir"])

(defn farmaco-component [{{:keys [historico nome prescricao-de-hoje]} :farmaco}]
  (let [idx-selecionado (or ((<sub [::receita-historico-selecionado]) nome) 0)
        historico-selecionado (as-> historico $
                                  (nth $ idx-selecionado)
                                  (clojure.string/split $ #"- ")
                                  (rest $)
                                  (clojure.string/join "- " $))]
    [:<>
     [titulo-farmaco nome]
     [:div
      {:style {:transform "translateY(-5px)"
               :position "relative"}}
      [historico-farmaco
       {:historico historico
        :nome nome}]]
     [:div
      {:style {:display "flex"
               :alignItems "center"}}
      [input-farmaco
       {:value prescricao-de-hoje
        :onBlur #(>evt [::set-todays-receita nome (-> % .-target .-value)])}]
      (when (empty? prescricao-de-hoje)
        [repetir-farmaco-button
         {:onClick #(>evt [::set-todays-receita nome historico-selecionado])}])]]))

(defn component []
  [:<>
   (for [farmaco (<sub [::farmacos-lista])]
     ^{:key (:nome farmaco)} [farmaco-component
                              {:farmaco farmaco}
                              ])])

(defn view []
  [shell/default
   [shell/error-boundary
    {:if-error [:h1 "Erro receita"]} ;; TODO: replace error-view
    [component]]])
