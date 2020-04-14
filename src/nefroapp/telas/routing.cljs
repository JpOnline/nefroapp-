(ns nefroapp.telas.routing
  "Responsável por gerenciar qual tela será visível."
  (:require
    [nefroapp.telas.lista-pacientes :as pacientes]
    [nefroapp.telas.receita :as receita]
    [nefroapp.util :as util :refer [<sub >evt]]
    [re-frame.core :as re-frame]
    ))

(re-frame/reg-sub
  ::state
  (fn [app-state] (get-in app-state [:ui :screen-state])))

(defn selected-view []
  (case (<sub [::state])
    "pacientes" [pacientes/view]
    "receita" [receita/view]))
