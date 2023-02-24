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
        query-index (->> queries
                         (map (fn [{:keys [query-key] :as q}]
                                [query-key q]))
                         (into {}))
        wrapper (fn [props]
                  (let [query-hooks (update-vals query-index #(use-query config %))
                        rf-db-loaded (->> queries
                                          (map #(deref (rf/subscribe [::query-state %])))
                                          (every? some?))]
                    (doseq [[query-key q] query-hooks]
                      (useEffect
                        (fn []
                          (let [query-def (get query-index query-key)
                                query-state {:status (keyword (.-status q))
                                             :data (.-data q)
                                             :error (.-error q)}]
                            (rf/dispatch [::query-state-changed query-def query-state]))
                          js/undefined)
                        #js[(.-status q) (.-data q) (.-error q)]))
                    (when rf-db-loaded
                      [render-fn props])))]
    (fn [props]
      [:f> wrapper props])))
