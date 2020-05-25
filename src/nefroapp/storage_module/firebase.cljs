(ns nefroapp.storage-module.firebase
  (:require
    [firebase]
    [oops.core :as oops]
    ))

(def firebase-config
  #js {:apiKey "AIzaSyAaFnmgQYCanR_fG2EkTJRNFdAAsrr8ojY"
       :authDomain "receita-nefro.firebaseapp.com"
       :databaseURL "https://receita-nefro.firebaseio.com"
       :projectId "receita-nefro"
       ;; :storageBucket ""
       ;; :messagingSenderId ""
       :appId "1:90181093295:web:35a89ec3ee054f2204115a"
       :measurementId "G-L12PX42LSM"})

(def firebase-db
  (do
    (when (= 0 js/firebase.apps.length)
      (js/firebase.initializeApp firebase-config))
    (js/firebase.database)))

(defn my-key->js [k]
  (cond
    (keyword? k) (str k)
    (number? k) k
    :default (clj->js k)))

(defn my-clj->js [x]
  (cond
    (satisfies? IEncodeJS x) (my-clj->js x)
    (keyword? x) (my-key->js x)
    (map? x) (let [m (js-obj)]
               (doseq [[k v] x]
                 (goog.object/set m (my-key->js k) (my-clj->js v)))
               m)
    (coll? x) (let [arr (array)]
                (doseq [x (map my-clj->js x)]
                  (.push arr x))
                arr)
    :else (clj->js x)))

(defn my-key->clj [k]
  (cond
    (= k (str (js/parseInt k))) (js/parseInt k)
    (= ":" (first k)) (->> k rest (apply str) keyword)
    :else k))

(defn my-js->clj [x]
  (cond
    (satisfies? IEncodeClojure x)
    (my-js->clj x)

    (seq? x)
    (doall (map my-js->clj x))

    (map-entry? x)
    (MapEntry. (my-js->clj (key x)) (my-js->clj (val x)) nil)

    (coll? x)
    (into (empty x) (map my-js->clj) x)

    (array? x)
    (persistent!
      (reduce #(conj! %1 (my-js->clj %2))
              (transient []) x))

    (identical? (type x) js/Object)
    (persistent!
      (reduce (fn [r k] (assoc! r (my-key->clj k) (my-js->clj (goog.object/get x k))))
              (transient {}) (js-keys x)))

    :else x))

(defn save! [path value]
  (let [json (my-clj->js value)]
    (assert
      (= value (my-js->clj json))
      "The given map is different if converted back from JSON.")
    (-> firebase-db (.ref path)
        (.set json #(when % (js/console.log "Erro ao gravar no Firebase." %))))))

(defn async-load [path callback-fn]
  (-> firebase-db (.ref path) (.once "value"
     (fn [snapshot]
       (callback-fn (some-> (oops/ocall snapshot "val")
                            (my-js->clj))))
     (fn [error]
       (js/console.log "Erro ao ler dados do Firebase." error)))))
