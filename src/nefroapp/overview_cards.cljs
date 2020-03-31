(ns nefroapp.overview-cards
  (:require
    [devcards.core :as devcards :refer-macros [defcard deftest defcard-rg]]
    [Button :as material-button]
    ))

;; Draw the devcards ui.
(devcards.core/start-devcard-ui!)

(defcard
  (str "# Primeiro Card")
  {}
  {:frame false
   :heading false})

(defcard-rg x
  [:> material-button
   "Teste"])

;; (defonce devcards-hidden (reagent/atom []))
;;
;; (defcard-rg hidding-cards
;;   (let [card-container-style #js {:display "flex"
;;                                   :justifyContent "space-evenly"
;;                                   :padding "20px 0"}]
;;     (fn [devcard-data _]
;;       [:div.card-container
;;        {:style card-container-style}
;;        [:> material-button
;;         {:id "hide-all"
;;          :variant "outlined"
;;          :size "small"
;;          :color "secondary"
;;          :onClick #(doseq [hidden-atom @devcards-hidden]
;;                      (reset! hidden-atom true))}
;;         "hide all"]
;;        [:> material-button
;;         {:id "show-all"
;;          :variant "outlined"
;;          :size "small"
;;          :color "secondary"
;;          :onClick #(doseq [hidden-atom @devcards-hidden]
;;                      (reset! hidden-atom false))}
;;         "show all"]]))
;;   {}
;;   {:frame false})

;; (defcard-rg mobile-view
;;   (fn [devcard-data _]
;;     (let [{:keys [hidden?]} @devcard-data]
;;       (swap! devcards-hidden conj hidden?)
;;       [:<>
;;        [:div.card-expander
;;         {:onClick #(swap! hidden? not)
;;          :style #js {:textAlign "center"}}
;;         (if @hidden?
;;           [:> material-icons-ExpandMore]
;;           [:> material-icons-ExpandLess])]
;;        [:div.card-container
;;         {:style #js {:display "flex"
;;                      ;; :justifyContent "space-evenly"
;;                      :paddingBottom 20}}
;;         [shell/error-boundary
;;          {:if-error [shell/error-view]}
;;          [:div.component-container
;;           {:hidden @hidden?
;;            :style #js {:width 360 :height 640
;;                        :border "1px solid #00000038"}}
;;           [routes/selected-view]]]]]))
;;   {:hidden? (reagent/atom false)})
;;
;; (defcard-rg desktop-view
;;   (fn [devcard-data _]
;;     (let [{:keys [hidden?]} @devcard-data]
;;       (swap! devcards-hidden conj hidden?)
;;       [:<>
;;        [:div
;;         {:style #js {:display "flex"
;;                      :justifyContent "space-evenly"
;;                      :padding "15px 0"}}
;;         [:div
;;          [:div
;;           {:class "com-rigsomelight-devcards-panel-heading com-rigsomelight-devcards-typog"
;;            :style #js {:width "90vw"}}
;;           [:a
;;            {:href "#"
;;             :style #js {:color "#666"}}
;;            "desktop-view"]]]]
;;        [:div.card-expander
;;         {:onClick #(swap! hidden? not)
;;          :style #js {:textAlign "center"}}
;;         (if @hidden?
;;           [:> material-icons-ExpandMore]
;;           [:> material-icons-ExpandLess])]
;;        [:div
;;         {:style #js {:display "flex"
;;                      :justifyContent "space-evenly"}}
;;         [:div
;;          [:div.card-container
;;           {:style #js {:width "96vw"}}
;;           [shell/error-boundary
;;            {:if-error [shell/error-view]}
;;            [:div.component-container
;;             {:hidden @hidden?
;;              :style #js {:width "96vw" :height "90vh"
;;                          :border "1px solid #00000038"}}
;;             [routes/selected-view]]]]]]]))
;;   {:hidden? (reagent/atom false)}
;;   {:frame false})


