(ns nefroapp.util
  (:require
    [re-frame.core :as re-frame]
    ))

;; Redef re-frame subscribe and dispatch for brevity
(def <sub (comp deref re-frame.core/subscribe))
(def >evt re-frame.core/dispatch)
