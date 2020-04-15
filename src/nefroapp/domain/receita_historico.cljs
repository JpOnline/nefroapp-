(ns nefroapp.domain.receita-historico
  (:require
    [clojure.spec.alpha :as spec]
    [clojure.spec.gen.alpha :as gen]
    [tick.alpha.api :as tick]
    ))

(defn extract-string [today]
  (fn [[last-value last-date & acc] date-value]
    (let [{:keys [date value]} date-value
          [_ year month day] (re-find #"(\d{4})\D(\d{1,2})\D(\d{1,2})" date)
          [_ lyear lmonth lday] (re-find #"(\d{4})\D(\d{1,2})\D(\d{1,2})" last-date)
          [_ current-year _ _] (re-find #"(\d{4})\D(\d{1,2})\D(\d{1,2})" today)
          l-date (str day"/"month (when (not= current-year year) (str "/"year)))
          current-date (str lday"/"lmonth (when (not= current-year lyear) (str "/"lyear)))
          new-string (if last-value
                        (str l-date"~"current-date" - "value)
                        (str "Desde "l-date" - "value))]
        (concat [value date] acc [new-string]))))

(defn compact-history [today history]
  (->> history
       (partition-by :value)
       (map last)
       (reduce (extract-string today) [nil ""])
       (drop 2)))

(def date-regex #"\d{4}-\d{2}-\d{2}")
(spec/def ::date (spec/and string? #(re-matches date-regex %)))
(spec/def ::value #{"100ml" "50ml" "200ml"})
(spec/def ::date-value (spec/keys :req-un [::date ::value]))
(spec/def ::history (spec/coll-of ::date-value :kind vector?))

(defn date-gen []
  (gen/fmap #(str (tick/+ (tick/date "2019-01-01") (tick/new-period % :days)))
            (gen/choose 0 730))) ;; 2 anos

(defn history-gen []
  (gen/fmap #(->> % (sort-by :date) reverse vec)
            (spec/gen ::history
                      {::date date-gen})))

(spec/def ::today #{"2020-12-31"})
(spec/def ::history-arg
  (spec/cat :today ::today
            :history ::history))

(defn history-custom-gens []
  (spec/gen ::history-arg {::history history-gen}))

(spec/fdef compact-history-with-custom-gens
           :args (spec/with-gen
                   ::history-arg
                   history-custom-gens))

(comment
  (gen/generate (spec/gen ::n))
  (gen/sample (spec/gen ::n))

  (gen/generate (gen/choose 0 730))
  (gen/sample (date-gen))

  (compact-history (gen/generate (history-custom-gens)))

  (spec/exercise-fn `compact-history 1
                    (spec/get-spec `compact-history-with-custom-gens))
  )
