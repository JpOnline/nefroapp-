(ns nefroapp.storage-module.local-storage
  (:require
    [cognitect.transit :as transit]))

(def transit-read-handlers (atom {}))
(def transit-write-handlers (atom {}))

(defn clj->json [x]
  (transit/write (transit/writer :json {:handlers @transit-write-handlers}) x))

(defn json->clj [x]
  (transit/read (transit/reader :json {:handlers @transit-read-handlers}) x))

(defn get-item [key]
  (-> key
      clj->json
      (as-> json-key
        (-> js/localStorage (.getItem json-key)))
      json->clj))

(defn set-item! [key value]
  (try
    (.setItem js/localStorage (clj->json key) (clj->json value))
    (catch js/Object e
      (js/console.log "Erro ao gravar em Local Storage." (.-message e)))))
