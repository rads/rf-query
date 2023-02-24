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
  (fn [db [_ {:keys [query-key] :as _query-def}]]
    (get-in db [:query-state query-key])))

(defn- use-query [config {:keys [query-key query-fn]}]
  (let [query-opts #js{:queryKey (clj->js query-key)
                       :queryFn query-fn}]
    ((:use-query-fn config) query-opts)))

(defn with-queries [queries render-fn]
  (let [config @current-config
        hooks (fn [_]
                (doseq [query-def queries]
                  (let [q (use-query config query-def)]
                    (useEffect
                      (fn []
                        (let [query-state {:status (keyword (.-status q))
                                           :data (.-data q)
                                           :error (.-error q)}]
                          (rf/dispatch [::query-state-changed query-def query-state]))
                        js/undefined)
                      #js[(.-status q) (.-data q) (.-error q)]))))]
    (fn [props]
      [:<>
       [:f> hooks]
       (let [rf-db-loaded (->> queries
                               (map #(deref (rf/subscribe [::query-state %])))
                               (every? some?))]
         (when rf-db-loaded
           [render-fn props]))])))
