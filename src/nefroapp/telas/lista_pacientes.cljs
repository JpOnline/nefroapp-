(ns nefroapp.telas.lista-pacientes
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
    [list :as material-list]
    [list-item :as material-list-item]
    [list-item-text :as material-list-item-text]
    [nefroapp.telas.shell-components :as shell]
    [nefroapp.util :as util :refer [<sub >evt]]
    [re-frame.core :as re-frame]
    [search-icon :as mui-icon-search]
    ))

(defn-traced select-paciente
  [app-state]
  (assoc-in app-state [:ui :screen-state] "receita"))
(re-frame/reg-event-db ::select-paciente select-paciente)

(defn component []
  [:<>
   [:paper-input
    {:style {:width "100%"
             :marginTop "-18px"}
     :label "Buscar"
     ;; :onFocus #(>evt [::clear-errors])
     ;; :value (<sub [::email])
     ;; :onBlur #(>evt [::set-login-property :email (-> % .-target .-value)])
     }
    [:> mui-icon-search
     {:slot "suffix"}]]
   [:> material-list
   (map-indexed #(with-meta %2 {:key %1})
                (repeat 2 [:> material-list-item
                            {:button true
                             :onClick #(>evt [::select-paciente])}
                            [:> material-list-item-text
                             {:primary "Waldemiro"
                              :secondary "Receita editada em: 03/02/2020"}]]))]]
  )

(defn view []
  [shell/default
   [component]])
