(ns nefroapp.util
  (:require
    [re-frame.core :as re-frame]
    ))

;; Redef re-frame subscribe and dispatch for brevity
(def <sub (comp deref re-frame.core/subscribe))
(def >evt re-frame.core/dispatch)

(defn yyyy-mm-dd->dd-mm-yyyy [date]
  (let [[_ year month day] (re-find #"(\d{4})\D(\d{1,2})\D(\d{1,2})" date)]
     (str day"/"month"/"year)))
