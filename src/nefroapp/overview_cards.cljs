(ns nefroapp.overview-cards
  (:require
    [ArrowBack :as mui-icon-arrow-back]
    [Button :as material-button]
    [ExpandLess :as mui-icon-expand-less]
    [ExpandMore :as mui-icon-expand-more]
    [IconButton :as material-icon-button]
    [MoreVert :as mui-icon-more-vert]
    [devcards.core :as devcards :refer-macros [defcard deftest defcard-rg]]
    [nefroapp.telas.lista-pacientes :as lista-pacientes]
    [nefroapp.telas.receita :as receita]
    [reagent.core :as reagent]

    [Paper :as material-paper]
    [MenuList :as material-menu-list]
    [MenuItem :as material-menu-item]
    [tick.alpha.api :as tick]
    [clojure.spec.alpha :as spec]
    [cljs.test :refer-macros [is testing]]
    [clojure.spec.gen.alpha :as gen]
    [clojure.test.check.generators] ;; Is it necessary?
    ))

;; Draw the devcards ui.
(devcards.core/start-devcard-ui!)

(defcard
  (str "# Oficina Nefro App!")
  {}
  {:frame false
   :heading false})

(defonce devcards-hidden (reagent/atom []))

(defcard-rg hidding-cards
  (let [card-container-style #js {:display "flex"
                                  :justifyContent "space-evenly"
                                  :padding "20px 0"}]
    (fn [devcard-data _]
      [:div.card-container
       {:style card-container-style}
       [:> material-button
        {:id "hide-all"
         :variant "outlined"
         :size "small"
         :color "secondary"
         :onClick #(doseq [hidden-atom @devcards-hidden]
                     (reset! hidden-atom true))}
        "hide all"]
       [:> material-button
        {:id "show-all"
         :variant "outlined"
         :size "small"
         :color "secondary"
         :onClick #(doseq [hidden-atom @devcards-hidden]
                     (reset! hidden-atom false))}
        "show all"]]))
  {}
  {:frame false})

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

(defn actions-menu [{:keys [actions]}]
  [:<> ;; Needed because can accept a possible nil
   (when false #_(<sub [::actions-opened?])
     [:<>
      [:div.close-menu-invisible-layer
       {:style {:position "absolute"
                :width "100%"
                :height "100%"
                :zIndex 1199
                :backgroundColor "#0000000d"}
        ;; :onClick #(>evt [::close-actions-menu])
        }]
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
    ;; :onClick #(>evt [::open-actions-menu])
    }
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
   ;; [actions-menu
   ;;  {:actions actions}]
   ])

(defcard-rg mobile-view
  (fn [devcard-data _]
    (let [{:keys [hidden?]} @devcard-data]
      (swap! devcards-hidden conj hidden?)
      [:<>
       [:div.card-expander
        {:onClick #(swap! hidden? not)
         :style #js {:textAlign "center"}}
        (if @hidden?
          [:> mui-icon-expand-more]
          [:> mui-icon-expand-more])]
       [:div.card-container
        {:style #js {:display "flex"
                     :paddingBottom 20}}
        [:div.component-container
         {:hidden @hidden?
          :style #js {:width 360 :height 640
                      :border "1px solid #00000038"}}
         [default [lista-pacientes/component]]
         ]]]))
  {:hidden? (reagent/atom false)})

(defcard-rg desktop-view
  (fn [devcard-data _]
    (let [{:keys [hidden?]} @devcard-data]
      (swap! devcards-hidden conj hidden?)
      [:<>
       [:div
        {:style #js {:display "flex"
                     :justifyContent "space-evenly"
                     :padding "15px 0"}}
        [:div
         [:div
          {:class "com-rigsomelight-devcards-panel-heading
                  com-rigsomelight-devcards-typog"
           :style #js {:width "90vw"}}
          [:a
           {:href "#"
            :style #js {:color "#666"}}
           "desktop-view"]]]]
       [:div.card-expander
        {:onClick #(swap! hidden? not)
         :style #js {:textAlign "center"}}
        (if @hidden?
          [:> mui-icon-expand-more]
          [:> mui-icon-expand-more])]
       [:div
        {:style #js {:display "flex"
                     :justifyContent "space-evenly"
                     :fontWeight "500"}}
        [:div
         [:div.card-container
          {:style #js {:width "96vw"}}
          [:div.component-container
           {:hidden @hidden?
            :style #js {:width "96vw" :height "90vh"
                        :border "1px solid #00000038"}}
           [default [lista-pacientes/component]]
           ]]]]]))
  {:hidden? (reagent/atom false)}
  {:frame false})

(defn card-component [& children]
  (fn card-component [devcard-data _]
    (let [{:keys [hidden?]} @devcard-data]
      (swap! devcards-hidden conj hidden?)
      [:<>
       [:div.card-expander
        {:onClick #(swap! hidden? not)
         :style #js {:textAlign "center"}}
        (if @hidden?
          [:> mui-icon-expand-more]
          [:> mui-icon-expand-less])]
       [:div.card-container
        {:style #js {:display "flex"}}
        [error-boundary
         {:if-error [:h1 "Erro no card-component. 🤔"]}
         [:div.component-container
          {:hidden @hidden?}
          (map-indexed #(with-meta %2 {:key %1}) children)]]]])))

(defcard-rg lista-pacientes-card
  (card-component
    [lista-pacientes/component])
  {:hidden? (reagent/atom false)})

(defcard-rg receita-card
  (card-component
    [receita/component])
  {:hidden? (reagent/atom false)})

(defn extract-string [today]
  (fn [[last-value last-date & acc] date-value]
    (let [{:keys [date value]} date-value
          [_ year month day] (re-find #"(\d{4})\D(\d{1,2})\D(\d{1,2})" date)
          [_ lyear lmonth lday] (re-find #"(\d{4})\D(\d{1,2})\D(\d{1,2})" last-date)
          [_ current-year _ _] (re-find #"(\d{4})\D(\d{1,2})\D(\d{1,2})" today)
          l-date (str day"/"month (when (not= current-year year) (str "/"year)))
          current-date (str lday"/"lmonth (when (not= current-year lyear) (str "/"lyear)))
          new-string (if last-value
                        (str l-date"~"current-date" - "value)
                        (str "Desde "l-date" - "value))]
        (concat [value date] acc [new-string]))))

(defn compact-history [today history]
  (->> history
       (partition-by :value)
       (map last)
       (reduce (extract-string today) [nil ""])
       (drop 2)))

;; ---------- SPECS for compact-history ----------
  (def date-regex #"\d{4}-\d{2}-\d{2}")
  (spec/def ::date (spec/and string? #(re-matches date-regex %)))
  (spec/def ::value #{"100ml" "50ml" "200ml"})
  (spec/def ::date-value (spec/keys :req-un [::date ::value]))
  (spec/def ::history (spec/coll-of ::date-value :kind vector?))

  (defn date-gen []
    (gen/fmap #(str (tick/+ (tick/date "2019-01-01") (tick/new-period % :days)))
              (gen/choose 0 730))) ;; 2 anos

  (defn history-gen []
    (gen/fmap #(->> % (sort-by :date) reverse vec)
              (spec/gen ::history
                        {::date date-gen})))

  (spec/def ::today #{"2020-12-31"})
  (spec/def ::history-arg
    (spec/cat :today ::today
              :history ::history))

  (defn history-custom-gens []
    (spec/gen ::history-arg {::history history-gen}))

  (spec/fdef compact-history-with-custom-gens
             :args (spec/with-gen
                     ::history-arg
                     history-custom-gens))

  (comment
    (gen/generate (spec/gen ::n))
    (gen/sample (spec/gen ::n))

    (gen/generate (gen/choose 0 730))
    (gen/sample (date-gen))

    (compact-history (gen/generate (history-custom-gens)))

    (spec/exercise-fn `compact-history 1
                      (spec/get-spec `compact-history-with-custom-gens))
    )

(deftest compact-history-example
  (testing "Dado que o médico repetiu a receita em pelo menos 2 datas."
    (testing "Quando mostrar histórico compactado."
      (is (= '("Desde 29/12 - 200ml" "14/03~29/12 - 100ml" "15/12/2019~14/03 - 50ml" "12/04/2019~15/12/2019 - 100ml" "06/04/2019~12/04/2019 - 200ml")
             (compact-history "2020-12-31" [ { :date "2020-12-29" :value "200ml" } { :date "2020-03-14" :value "100ml" } { :date "2020-01-26" :value "50ml" } { :date "2019-12-15" :value "50ml" } { :date "2019-04-12" :value "100ml" } { :date "2019-04-06" :value "200ml"}]))
          "Então serão mostradas apenas as datas de quando o paciente começou e terminou aquela receita."))))

(defcard exercise-fn-compact-history
  (spec/exercise-fn `compact-history 1
                    (spec/get-spec `compact-history-with-custom-gens)))

