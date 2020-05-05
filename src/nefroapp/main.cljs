(ns ^:figwheel-hooks nefroapp.main
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
    [nefroapp.telas.routing :as routing]
    [re-frame.core :as re-frame]
    [reagent.dom]
    ))

(defn register-service-worker! []
  (.addEventListener js/window "load" #(some-> js/navigator
                                               .-serviceWorker
                                               (.register "/nefroapp/sw.js"))))

(def initial-state
  {:domain
   {:pacientes
    [{:id 0
      :nome "João Paulo Soares"
      :receitas '({:criada-em "2020-04-19T11:07:38.106-03:00[SYSTEM]"
                   :editada-em "2020-04-13T11:07:38.106-03:00[SYSTEM]"
                   :farmacos {"Hidróxido de Ferro" {:prescricao "100ml"}}}
                  {:criada-em "2020-02-14T11:07:38.106-03:00[SYSTEM]"
                   :editada-em "2020-04-13T11:07:38.106-03:00[SYSTEM]"
                   :farmacos {"Hidróxido de Ferro" {:prescricao "2"}}}
                  )}]}
   :ui {:screen-state "receita"}})

(re-frame/reg-event-db ::set-app-state
  (fn-traced [_ [event application-state]]
             application-state))

(defn init-state! []
  (re-frame/dispatch-sync [::set-app-state initial-state]))

(defn ^:after-load mount-app-element! []
  (when-let [el (.getElementById js/document "app")]
    (reagent.dom/render [routing/selected-view] el)))

(defonce startup
  (do
    (register-service-worker!)
    (init-state!)
    (mount-app-element!)))
