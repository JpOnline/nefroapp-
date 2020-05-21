(ns nefroapp.util
  (:require
    [re-frame.core :as re-frame]
    [reagent.dom]
    [tick.alpha.api :as tick]
    ))

;; Redef re-frame subscribe and dispatch for brevity
(def <sub (comp deref re-frame.core/subscribe))
(def >evt re-frame.core/dispatch)

(defn yyyy-mm-dd->dd-mm-yyyy [date]
  (let [[_ year month day] (re-find #"(\d{4})\D(\d{1,2})\D(\d{1,2})" date)]
     (str day"/"month"/"year)))

(defonce already-defined-components (atom {})) ;; This is used for hot reloading.
(defn define-custom-element! [name view-component]
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

(defn today [] (str (tick/zoned-date-time)))
