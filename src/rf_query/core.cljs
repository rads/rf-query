(ns rf-query.core
  (:require [re-frame.core :as rf]
            ["react" :refer [useEffect]]))

(def current-config (atom nil))

(defn set-config! [& {:as opts}]
  (let [new-config (select-keys opts [:use-query-fn
                                      :use-mutation-fn])]
    (reset! current-config new-config)))

(rf/reg-event-db
  ::query-state-changed
  (fn [db [_ query-def query-state]]
    (update-in db [:query-state (:query-key query-def)]
               merge query-state)))

(rf/reg-sub
  ::query-state
  (fn [db [_ {:keys [query-key] :as _query-def}]]
    (get-in db [:query-state query-key])))

(defn- use-query [config {:keys [query-key query-fn initial-data stale-time]}]
  (let [query-opts #js{:queryKey (clj->js query-key)
                       :queryFn query-fn
                       :initialData initial-data
                       :staleTime stale-time}]
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
       [render-fn props]
       [:f> hooks]])))

(def ^:private mutation-listeners (atom {}))

(defn- add-mutation-listener [id f]
  (swap! mutation-listeners assoc id f))

(defn- mutate! [mutation]
  (doseq [[_ f] @mutation-listeners]
    (f mutation)))

(rf/reg-fx ::mutate mutate!)

(rf/reg-event-fx
  ::mutate
  (fn [_ [_ mutation]]
    {:fx [[::mutate mutation]]}))

(defn- use-mutation [config {:keys [mutation-fn on-success]}]
  (let [mutation-opts #js{:mutationFn mutation-fn
                          :onSuccess on-success}]
    ((:use-mutation-fn config) mutation-opts)))

(defn provider [{:keys [mutations]} & children]
  (let [config @current-config
        hooks (fn [_]
                (let [m (->> mutations
                             (map (fn [mutation-def]
                                    [mutation-def (use-mutation config mutation-def)]))
                             (into {}))]
                  (useEffect
                    (fn []
                      (let [id (gensym)]
                        (add-mutation-listener id #(.mutate (get m %))))
                      js/undefined)
                    #js[])))]
    (into [:<> [:f> hooks]]
          children)))
