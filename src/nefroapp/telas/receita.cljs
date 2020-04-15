(ns nefroapp.telas.receita
  (:require
    [Button :as material-button]
    [ChevronLeftIcon :as mui-icon-chevron-left]
    [ChevronRightIcon :as mui-icon-chevron-right]
    [IconButton :as material-icon-button]
    [nefroapp.domain.receita-historico :as receita-historico]
    [nefroapp.telas.shell-components :as shell]
    [nefroapp.util :as util :refer [<sub >evt]]
    [re-frame.core :as re-frame]
    ;; [List :as material-list]
    ;; [ListItem :as material-list-item]
    ;; [ListItemText :as material-list-item-text]
    ;; [SearchIcon :as mui-icon-search]

    [reagent.core :as reagent]
    ))

(defn ultima-receita
  [app-state]
  (let [paciente-selecionado (get-in app-state [:ui :paciente-selecionado] 0)
        receitas (get-in app-state [:domain :pacientes paciente-selecionado :receitas])
        sorted-receitas (reverse (sort-by :criada-em receitas)) ;; TODO: Precisa ordenar? Avaliar.

        ;; Cria algo do tipo
          ;; {"farmacoA" [{:data "2020-01-01" :value "10ml"}
          ;;              {:data "2020-02-01" :value "20ml"}]
          ;;  "farmacoB" [{:data "2020-01-01" :value "50ml"}
          ;;              {:data "2020-02-01" :value "70ml"}]}
        farmacos->date-values-map (fn [criada-em farmacos]
                                    (reduce (fn [acc {:keys [nome prescricao]}]
                                              (merge acc {nome [{:date criada-em :value prescricao}]}))
                                            {} farmacos))

        farmacos-history (reduce (fn [acc {:keys [criada-em farmacos]}]
                                   (merge-with conj acc
                                               (farmacos->date-values-map criada-em farmacos)))
                                 {} sorted-receitas)
           ;; TODO: Colocar função pra pegar a data de hoje
        add-farmacos-history #(map (fn [farmaco] (merge farmaco {:historico (receita-historico/compact-history "2020-04-14" (farmacos-history (:nome farmaco)))})) %)]
    (update (first sorted-receitas) :farmacos add-farmacos-history)))
(re-frame/reg-sub
  ::ultima-receita
  ultima-receita)

(def left {:display "flex"
           :marginBottom "7px"
           :position "absolute"
           :transform "translateX(-400px)"
           :clipPath "inset(0px 0px 0px 400px)"
           :transition "0.8s"})

(def middle {:display "flex"
             :marginBottom "7px"
             :transform "translateX(0px)"
             :position "absolute"
             :clipPath "inset(0px)"
             :transition "0.8s"})

(def right {:display "flex"
            :marginBottom "7px"
            :position "absolute"
            :transform "translateX(400px)"
            :clipPath "inset(0px 400px 0px 0px)"
            :transition "0.8s"})

(def p1 (reagent/atom left))
(def p2 (reagent/atom middle))

(defn titulo-medicamento [titulo]
  [:h3
   {:style {:marginBottom "5px"}}
   titulo])

(defn historico-medicamento [{:keys [position-state]} text]
  [:div
   {:style position-state}
   [:> material-icon-button
    {:color "inherit"
     :onClick #(do (reset! p1 middle)
                   (reset! p2 right))}
    [:> mui-icon-chevron-left]]
   [:p {:style {:color "gray"}}
    text]
   [:> material-icon-button
    {:color "inherit"
     :onClick #(do (reset! p1 left)
                   (reset! p2 middle))}
    [:> mui-icon-chevron-right]]])

(defn input-medicamento []
  [:div
   {:style {:border "1px solid #BDBDBD"
            :borderRadius "8px"
            :padding "0px 3px"}}
   [:paper-input
    {:style {:width "100%"
             :marginTop "-18px"}
     ;; :label "Buscar"
     ;; :onFocus #(>evt [::clear-errors])
     ;; :value (<sub [::email])
     ;; :onBlur #(>evt [::set-login-property :email (-> % .-target .-value)])
     }]])

(defn repetir-medicamento-button []
  [:> material-button
   {:style {:marginLeft "10px"}
    :variant "contained"
    :size "small"}
   "Repetir"])

(defn medicamento-component [{:keys [nome]}]
  [:<>
   [titulo-medicamento nome]
   [:div
    {:style {:transform "translateY(-5px)"
             :position "relative"}}
    [historico-medicamento
     {:position-state @p1}
     "05/01~02/02 - 100ml"]
    [historico-medicamento
     {:position-state @p2}
     "Desde 03/02/2020 - Sem Receber"]]
   [:p {:style {:padding "0px 48px"
                :visibility "hidden"
                :transition "max-height 4s ease-in-out"
                :overflow "hidden"
                :height "auto"
                :maxHeight "100%"
                ;; :maxHeight "0px"
                }}
    (if (= left @p1)
      "Desde 03/02/2020 - Sem RecebeDesde 03/02/2020 - Sem ReceberrDesde 03/02/2020 - Sem Receber"
      "05/01~02/02 - 100ml")]
   [:div
    {:style {:display "flex"
             :alignItems "center"}}
    [input-medicamento]
    [repetir-medicamento-button]]]
  )

(defn component []
  [:<>
   (for [farmaco (:farmacos (<sub [::ultima-receita]))]
     ^{:key (:nome farmaco)} [medicamento-component
                              {:nome (:nome farmaco)}
                              ])])

(defn view []
  [shell/default
   [component]])
