(ns nefroapp.overview-cards
  (:require
    [Button :as material-button]
    [ExpandLess :as mui-icon-expand-less]
    [ExpandMore :as mui-icon-expand-more]
    [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
    [devcards.core :as devcards :refer-macros [defcard deftest defcard-rg]]
    [nefroapp.telas.lista-pacientes :as lista-pacientes]
    [nefroapp.telas.receita :as receita]
    [nefroapp.telas.routing :as routing]
    [nefroapp.domain.receita-historico :as receita-historico]
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
      :receitas '({:criada-em "2020-04-13T11:07:38.106-03:00[SYSTEM]"
                   :editada-em "2020-04-13T11:07:38.106-03:00[SYSTEM]"
                   :farmacos [{:nome "Hidróxido de Ferro"
                               :prescricao "100ml"}]})}]}
   :ui {:screen-state "pacientes"}})

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


(deftest compact-history-example
  (testing "Dado que o médico repetiu a receita em pelo menos 2 datas."
    (testing "Quando mostrar histórico compactado."
      (is (= '("Desde 29/12 - 200ml" "14/03~29/12 - 100ml" "15/12/2019~14/03 - 50ml" "12/04/2019~15/12/2019 - 100ml" "06/04/2019~12/04/2019 - 200ml")
             (receita-historico/compact-history "2020-12-31" [ { :date "2020-12-29" :value "200ml" } { :date "2020-03-14" :value "100ml" } { :date "2020-01-26" :value "50ml" } { :date "2019-12-15" :value "50ml" } { :date "2019-04-12" :value "100ml" } { :date "2019-04-06" :value "200ml"}]))
          "Então serão mostradas apenas as datas de quando o paciente começou e terminou aquela receita."))))

(defcard exercise-fn-compact-history
  (spec/exercise-fn `receita-historico/compact-history 1
                    (spec/get-spec `receita-historico/compact-history-with-custom-gens)))

;; ---------- SPECS for app-state ----------
  (spec/def ::prescricao #{"125ml" "250ml" "600ml" "Infundir 300ml (Máximo: 600ml)" "300mg - 3 vezes por semana" "6mg" "500ml/min" "300ml/min" "Via oral: 0,25-0,5µg/dia" "Via oral: 0,5-1µg 3 vezes por semana" "0,5-1µg de 2-3 vezes por semana" "Via intravenosa: 1-4µg, 3 vezes por semana, após a diálise"})
  (spec/def :farmaco/nome #{"Hidróxido de Ferro" "Eritropoetina" "Heparina" "Darbepoietina" "Enoxaparina" "Manitol" "Amido Hidroxietílico" "Gabapentina" "Dexclorfeniramina" "Calcitriol"})
  (spec/def ::farmaco (spec/keys :req-un [:farmaco/nome ::prescricao]))
  (spec/def ::farmacos (spec/coll-of ::farmaco :kind vector? :max-count 8))
  (def zoned-date-time-regex #"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}")
  (spec/def ::zoned-date-time (spec/and string? #(re-find zoned-date-time-regex %)))
  (spec/def ::editada-em ::zoned-date-time)
  (spec/def ::criada-em ::zoned-date-time)
  (spec/def ::receita (spec/keys :req-un [::criada-em ::editada-em ::farmacos]))
  (spec/def ::receitas (spec/coll-of ::receita :kind list? :max-count 100))
  (spec/def ::nome #{"Waldomiro Donaire" "Raul Araujo" "Anderson Siqueira" "Marcos Silva" "Fernanda Ramos" "Roberta Lima" "João Alves" "Rodrigo Pereira" "Aline Ribeiro" "Abel Tavares" "Sandra Castro"})
  (spec/def ::id nat-int?)
  (spec/def ::paciente (spec/keys :req-un [::id ::nome ::receitas]))
  (spec/def ::pacientes (spec/coll-of ::paciente :kind vector? :max-count 2))
  (spec/def ::ui #{{:screen-state "receita"}})
  (spec/def ::domain (spec/keys :req-un [::pacientes]))
  (spec/def ::app-state (spec/keys :req-un [::domain ::ui]))

  (defn zoned-date-time-gen []
    (gen/fmap
      (fn [{:keys [d m]}]
        (str (tick/+ (tick/zoned-date-time "2019-01-01T06:00:00.000-03:00[SYSTEM]") (tick/new-period d :days) (tick/new-duration m :millis))))
      (gen/hash-map :d (gen/choose 0 730) :m (gen/choose 0 64700000)))) ;; 2 anos e 17 horas no máximo

(comment
  (nefroapp.overview-cards/reset-state! nefroapp.overview-cards/initial-state)
  (gen/generate (spec/gen ::app-state {::zoned-date-time zoned-date-time-gen}))
  (nefroapp.overview-cards/reset-state! (gen/generate (spec/gen ::app-state {::zoned-date-time zoned-date-time-gen})))
  )
