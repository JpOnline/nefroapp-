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


(defonce already-defined-components (atom {})) ;; This is used for hot reloading.
(defn webcomponent! [name view-component]
  (if (js/window.customElements.get name)
    (swap! already-defined-components #(assoc % name view-component))
    (let [set-shadow #(do (set! (.-shadow %) (.attachShadow % #js {:mode "open"})) %)
          _ (swap! already-defined-components #(assoc % name view-component))
          render #(do (reagent.dom/render [(@already-defined-components name)] (.-shadow %)) %)
          ;; defines the constructor function, which is the "class" object used by the customElements api
          component (fn component []
                      (-> (js/Reflect.construct js/HTMLElement #js [] component)
                          set-shadow
                          render))]
      (set! (.-prototype component)
            ;; establishes prototype hierarchy
            (js/Object.create (.-prototype js/HTMLElement) #js {}))

      ;;finally, defines the component with these values
      (js/window.customElements.define name component))))

(defn m-component []
  [:div
   [:style
    "* {color: blue;}"]
   [:h1 "Ex 11"]])

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
   [:k-component]
   [:h1 "Exemplo"]
   ;; [:x-component]
   ;; [m-component]
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
