package com.example.dormatryapp

import androidx.lifecycle.MutableLiveData
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.util.GeoPoint


class RoutingTask(
    val liveData: MutableLiveData<ArrayList<GeoPoint>>,
    val roadManager: RoadManager,
    val list: ArrayList<GeoPoint>
) : Thread() {

    override fun run() {
        super.run()
        val road = roadManager.getRoad(list)
        val roadOverly = RoadManager.buildRoadOverlay(road)
        liveData.postValue(roadOverly.points)
    }
}