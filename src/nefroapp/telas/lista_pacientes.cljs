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

    [reagent.dom]
    ))

(defn-traced select-paciente
  [app-state]
  (assoc-in app-state [:ui :screen-state] "receita"))
(re-frame/reg-event-db ::select-paciente select-paciente)

(defn pacientes-e-receitas-data
  [app-state]
  (let [pacientes (get-in app-state [:domain :pacientes] {})
        ultima-fn (fn [{:keys [receitas]}]
                    (->> receitas
                         (map :editada-em)
                         sort
                         last
                         util/yyyy-mm-dd->dd-mm-yyyy))]
    (map #(assoc % :ultima-receita-editada (ultima-fn %)) pacientes)))
(re-frame/reg-sub ::pacientes-e-receitas-data pacientes-e-receitas-data)

(defn component []
  (js/Reflect.construct js/HTMLElement #js [] component))

(set! (.-prototype component)
  (js/Object.create (.-prototype js/HTMLElement)
    #js {:connectedCallback
         #js {:configurable true
              :value
              (fn []
                (this-as this
                  (set! (.-innerHTML this) "<h2>Exemplo6</h2>")
                  (js/console.log "connected" this)))}
         }))

(when-not (js/window.customElements.get "x-component")
  (js/window.customElements.define "x-component" component))

(defn webcomponent! [name view-component]
  (let [;; defines the constructor function, which is the "class" object used by the customElements api
        component (fn component [] (let [e
                                         ;; this is the equivalent of the call to "super"
                                         (js/Reflect.construct js/HTMLElement #js [] component)]
                           (js/console.log (str name ": Constructed!"))
                           (set! (.-shadow e) (.attachShadow e #js {:mode "open"}))
                           e))]
    (set! (.-prototype component)
          ;; establishes prototype hierarchy
          (js/Object.create (.-prototype js/HTMLElement)
                            #js {:connectedCallback
                                 #js {:configurable true
                                      :value        (fn []
                                                      (this-as this
                                                        ;; attaches the reagent process to the shadow dom
                                                        (reagent.dom/render [view-component] (.-shadow this))
                                                        (js/console.log (str name ": Connected! ") this)))}}))

    ;;finally, defines the component with these values
    (js/window.customElements.define name component)

    component))

(defn m-component []
  [:div
   [:style
    "* {color: blue;}"
    ]
   [:h1 "Ex 2"]])

(webcomponent! "k-component" m-component)

(defn component-l []
  [:<>
   [:paper-input
    {:style {:width "100%"
             :marginTop "-18px"}
     :label "Buscar"
     ;; :onFocus #(>evt [::clear-errors])
     ;; :value (<sub [::email])
     ;; :onBlur #(>evt [::set-login-property :email (-> % .-target .-value)])
     :onClick #(js/alert "😑 Funcionalidade não disponível ainda.")}
    [:> mui-icon-search
     {:slot "suffix"}]]
   [:h1 "Exemplo"]
   [:x-component]
   [:k-component]
   [:> material-list
    (when (empty? (<sub [::pacientes-e-receitas-data]))
      [:p "Sem pacientes cadastrados ainda."])
    (for [{:keys [id nome ultima-receita-editada]}
          (<sub [::pacientes-e-receitas-data])]
      ^{:key id}
      [:> material-list-item
       {:button true
        :onClick #(>evt [::select-paciente])}
       [:> material-list-item-text
        {:primary nome
         :secondary (str "Receita editada em: "ultima-receita-editada)}]])]])

(defn view []
  [shell/default
   [component-l]])
