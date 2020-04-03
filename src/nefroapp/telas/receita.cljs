(ns nefroapp.telas.receita
  (:require
    [Button :as material-button]
    [IconButton :as material-icon-button]
    [ChevronLeftIcon :as mui-icon-chevron-left]
    [ChevronRightIcon :as mui-icon-chevron-right]
    ;; [List :as material-list]
    ;; [ListItem :as material-list-item]
    ;; [ListItemText :as material-list-item-text]
    ;; [SearchIcon :as mui-icon-search]
    ))

(defn component []
  [:<>
   [:h3
    {:style {:marginBottom "5px"}}
    "Hidróxido de Ferro"]
   [:div
    {:style {:display "flex"
             :marginBottom "7px"}}
    [:> material-icon-button
     {:color "inherit"
      ;; :onClick #(>evt [::open-actions-menu])
      }
     [:> mui-icon-chevron-left]]
    [:p
     {:style {:color "gray"}}
     "Desde 03/02/2020 - Sem Receber"]
    [:> material-icon-button
     {:color "inherit"
      ;; :onClick #(>evt [::open-actions-menu])
      }
     [:> mui-icon-chevron-right]]
    ]
   [:div
    {:style {:display "flex"
             :alignItems "center"
             }}
    [:div
     {:style {:border "1px solid #BDBDBD"
              :borderRadius "8px"
              :padding "0px 3px"
              }}
     [:paper-input
      {:style {:width "100%"
               :marginTop "-18px"}
       ;; :label "Buscar"
       ;; :onFocus #(>evt [::clear-errors])
       ;; :value (<sub [::email])
       ;; :onBlur #(>evt [::set-login-property :email (-> % .-target .-value)])
       }
      ]]
    [:> material-button
     {:style {:marginLeft "10px"}
      :variant "contained"
      :size "small"
      }
     "Repetir Anterior"
     ]]
   #_[:> material-list
   (map-indexed #(with-meta %2 {:key %1})
                (repeat 15 [:> material-list-item
                            {:button true}
                            [:> material-list-item-text
                             {:primary "Waldemiro"
                              :secondary "Receita editada em: 03/02/2020"
                              }]]))]]
  )
