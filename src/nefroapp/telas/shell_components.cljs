(ns nefroapp.telas.shell-components
  "Reúne componentes básicos comuns a maioria das telas."
  (:require
    [IconButton :as material-icon-button]
    [MenuItem :as material-menu-item]
    [MenuList :as material-menu-list]
    [MoreVert :as mui-icon-more-vert]
    [Paper :as material-paper]
    [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
    [nefroapp.util :as util :refer [<sub >evt]]
    [re-frame.core :as re-frame]
    [reagent.core :as reagent]
    ))

(defn error-boundary []
  (let [error (reagent/atom nil)]
    (reagent/create-class
      {:component-did-catch (fn [this e info]) ;; For side effects, like logging the error.
       :get-derived-state-from-error (fn [e]
                                       (reset! error e)
                                       #js {})
       :reagent-render (fn [{:keys [if-error]} & children]
                         (if @error
                           [:<> if-error]
                           [:<> (map-indexed #(with-meta %2 {:key %1}) children)]))})))

(defn main-panel [& children]
  [:div.main-panel
   {:style #js {:display "flex"
                :position "relative" ;; So the childs can be positioned using absolute.
                :justifyContent "space-between"
                :flexDirection "column"
                :height "inherit"
                :width "inherit"}}
   [error-boundary
    {:if-error [:h1 "Erro"]} ;; TODO: mudar componente de erro.
    (map-indexed #(with-meta %2 {:key %1}) children)]])

(defn header [& children]
  [:header
   {:style #js {:color "white"
                :backgroundColor "#44736e"
                :width "100%"
                :display "flex"
                :justifyContent "space-between"
                :zIndex 1100
                :boxShadow "0px 2px 4px -1px #0003,
                            0px 4px 5px 0px #00000024,
                            0px 1px 10px 0px #0000001f"}}
   (map-indexed #(with-meta %2 {:key %1}) children)])

(defn top-bar [& children]
  [:div.top-bar
   {:style #js {:minHeight 64
                :padding "0 24px"
                :display "flex"
                :alignItems "center"}}
   (map-indexed #(with-meta %2 {:key %1}) children)])

(defn screen-title [& title-strs]
  [:h6
   {:style #js {:fontSize "1.25rem"
                :fontWeight 500
                :lineHeight 1.6
                :letterSpacing "0.0075em"
                :margin 0}}
   title-strs])

(defn footer []
  [:footer
   {:style #js {:color "white"
                :height 48
                :background "radial-gradient(circle, rgba(251,251,251,1) -30%, #44736e 110%)"
                :padding "0 24px"
                :display "flex"
                :justifyContent "center"
                :zIndex 1100
                :alignItems "center"}}
   [:img
    {:src "images/logoimagem-consultoria.webp"
     :height 40}]])

(defn main-content [& children]
  [:main
   {:style {:overflow "auto"
            :display "flex"
            :flexDirection "column"
            :height "100%"}}
   [:div.main-content
    {:style {:flexGrow 1
             :padding 12}}
    [error-boundary
     {:if-error [:h1 "Erro main-content"]} ;; TODO: replace error-view
     (map-indexed #(with-meta %2 {:key %1}) children)]]
   [footer]])

(defn-traced open-actions-menu
  [app-state]
  (assoc-in app-state [:ui :actions-menu :opened?] true))
(re-frame/reg-event-db ::open-actions-menu open-actions-menu)

(defn-traced close-actions-menu
  [app-state]
  (assoc-in app-state [:ui :actions-menu :opened?] false))
(re-frame/reg-event-db ::close-actions-menu close-actions-menu)

(defn actions-opened?
  [app-state]
  (get-in app-state [:ui :actions-menu :opened?]))
(re-frame/reg-sub ::actions-opened? actions-opened?)

(defn actions-menu [{:keys [actions]}]
  [:<> ;; Needed because can accept a possible nil
   (when (<sub [::actions-opened?])
     [:<>
      [:div.close-menu-invisible-layer
       {:style {:position "absolute"
                :width "100%"
                :height "100%"
                :zIndex 1199
                :backgroundColor "#0000000d"}
        :onClick #(>evt [::close-actions-menu])}]
      [:> material-paper
       {:elevation 8
        :style {:backgroundColor "white"
                :position "absolute"
                :right "12px"
                :top "20px"
                :zIndex 1200}}
       [:> material-menu-list
        (map (fn [action]
               ^{:key (action :name)}
               [:> material-menu-item
                ;; {:onClick #(>evt [(action :event)])}
                (action :name)])
             actions)]]])])

(defn actions-menu-icon []
  [:> material-icon-button
   {:color "inherit"
    :onClick #(>evt [::open-actions-menu])}
   [:> mui-icon-more-vert]])

(defn left-icon []
  #_[:> material-icon-button
   {:color "inherit"
    ;; :onClick #(>evt [:back]) ;; TODO: use sub to know which event to use
    }
   [:> mui-icon-arrow-back]]
  [:div.left-icon-placeholder
   {:style {:width "48px"}}]
  )

(defn default [& children]
  [main-panel
   [actions-menu
    {:actions [{:name "Novo Paciente"}
               {:name "Ação 1"}
               {:name "Ação 2"}
               {:name "Ação 3"}
               {:name "Ação 4"}
               ]}
    ]
   [header
    [left-icon]
    [top-bar
     [screen-title "Pacientes"]]
    [actions-menu-icon]
    ]
   [main-content
    (map-indexed #(with-meta %2 {:key %1}) children)]
   ])

