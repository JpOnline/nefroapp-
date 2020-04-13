(ns nefroapp.overview-cards
  (:require
    [ArrowBack :as mui-icon-arrow-back]
    [Button :as material-button]
    [ExpandLess :as mui-icon-expand-less]
    [ExpandMore :as mui-icon-expand-more]
    [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
    [devcards.core :as devcards :refer-macros [defcard deftest defcard-rg]]
    [nefroapp.telas.lista-pacientes :as lista-pacientes]
    [nefroapp.telas.receita :as receita]
    [nefroapp.telas.routing :as routing]
    [nefroapp.telas.shell-components :as shell]
    [re-frame.core :as re-frame]
    [reagent.core :as reagent]

    [tick.alpha.api :as tick]
    [cljs.test :refer-macros [is testing]]
    [clojure.spec.gen.alpha :as gen]
    [clojure.test.check.generators] ;; Is it necessary?
    [clojure.spec.alpha :as spec]
    ))

(defn reset-state!
  "Used to set tests initial state."
  [state]
  (re-frame/dispatch-sync [::set-app-state
                           (js->clj state :keywordize-keys true)]))

(re-frame/reg-event-db ::set-app-state
  (fn-traced [_ [event application-state]]
             application-state))

(def initial-state
  {:domain
   {:pacientes
    [{:id 0
      :nome "João Paulo Soares"
      :receitas [{:criada-em "2020-04-13T11:07:38.106-03:00[SYSTEM]"
                  :editada-em "2020-04-13T11:07:38.106-03:00[SYSTEM]"
                  :farmacos [{:nome "Hidróxido de Ferro"
                              :prescricao "100ml"}]}]}]}
   :ui {:screen-state "receita"}})

(defonce init-app-state
  (do
    (re-frame/dispatch-sync [::set-app-state initial-state])))
(re-frame/clear-subscription-cache!)

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
         [routing/selected-view]]]]))
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
           [routing/selected-view]]]]]]))
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
        [shell/error-boundary
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

(comment
  (nefroapp.overview-cards/reset-state! nefroapp.overview-cards/initial-state)
  )
