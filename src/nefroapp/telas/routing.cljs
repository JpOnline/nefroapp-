(ns nefroapp.telas.routing
  "Responsável por gerenciar qual tela será visível."
  (:require
    [nefroapp.telas.receita :as receita]
    ))

(defn selected-view []
  [receita/view])
