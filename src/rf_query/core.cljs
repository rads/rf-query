(ns rf-query.core
  (:require [re-frame.core :as rf]
            ["react" :refer [useEffect]]))

(def current-config (atom nil))

(defn set-config! [& {:as opts}]
  (reset! current-config (select-keys opts [:use-query-fn])))

(rf/reg-event-db
  ::query-state-changed
  (fn [db [_ query-def query-state]]
    (update-in db [:query-state (:query-key query-def)]
               merge query-state)))

(rf/reg-sub
  ::query-state
  (fn [db [_ query-key]]
    (get-in db [:query-state query-key])))

(defn- use-query [config opts]
  ((:use-query-fn config) (clj->js opts)))

(defn subscribe [query]
  (if-not (:query-key query)
    (rf/subscribe query)
    (let [{:keys [query-key query-fn]} query
          config @current-config
          query-opts {:queryKey query-key :queryFn query-fn}
          q (use-query config query-opts)]
      (useEffect
        (fn []
          (let [query-state {:status (keyword (.-status q))
                             :data (.-data q)
                             :error (.-error q)}]
            (rf/dispatch [::query-state-changed query query-state]))
          js/undefined)
        #js[(.-status q) (.-data q) (.-error q)])
      (rf/subscribe [::query-state query-key]))))
