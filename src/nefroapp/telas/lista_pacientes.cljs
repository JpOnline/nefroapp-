(ns nefroapp.telas.lista-pacientes
  (:require
    [List :as material-list]
    [ListItem :as material-list-item]
    [ListItemText :as material-list-item-text]
    [SearchIcon :as mui-icon-search]
    [nefroapp.telas.shell-components :as shell]
    ))

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
                (repeat 15 [:> material-list-item
                            {:button true}
                            [:> material-list-item-text
                             {:primary "Waldemiro"
                              :secondary "Receita editada em: 03/02/2020"
                              }]]))]]
  )

(defn view []
  [shell/default
   [component]])
