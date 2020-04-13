(ns nefroapp.telas.receita
  (:require
    [Button :as material-button]
    [IconButton :as material-icon-button]
    [ChevronLeftIcon :as mui-icon-chevron-left]
    [ChevronRightIcon :as mui-icon-chevron-right]
    [nefroapp.telas.shell-components :as shell]
    ;; [List :as material-list]
    ;; [ListItem :as material-list-item]
    ;; [ListItemText :as material-list-item-text]
    ;; [SearchIcon :as mui-icon-search]

    [reagent.core :as reagent]
    ))

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
   "Repetir Anterior"])

(defn component []
  [:<>
   [titulo-medicamento "Hidróxido de Ferro"]
   [:div
    {:style {:height "50px"}}
    [historico-medicamento
     {:position-state @p1}
     "05/01~02/02 - 100ml"]
    [historico-medicamento
     {:position-state @p2}
     "Desde 03/02/2020 - Sem Receber"]]
   [:div
    {:style {:display "flex"
             :alignItems "center"}}
    [input-medicamento]
    [repetir-medicamento-button]]])

(defn view []
  [shell/default
   [component]])
