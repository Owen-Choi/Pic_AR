//package com.mapbox.vision.examples
//
//import com.mapbox.vision.ar.FenceVisualParams
//import com.mapbox.vision.ar.LaneVisualParams
//import com.mapbox.vision.ar.VisionArManager
//import com.mapbox.vision.ar.core.models.Color
//import com.mapbox.vision.ar.view.gl.VisionArView
//
//class ArCustomizationActivityKt : ArActivityKt() {
//
//    override fun setArRenderOptions(visionArView: VisionArView) {
//        // Make sure lane is visible
//        visionArView.setLaneVisible(true)
//        // Set the desired lane length in meters.
//        VisionArManager.setLaneLength(40f)
//
//        val laneVisualParams = LaneVisualParams(
//            // Set red lane color
//            color = Color(1.0f, 0.0f, 0.0f, 1.0f),
//            // Set lane width in meters
//            width = 1f,
//            // Set the length of the chevrons in meters
//            arrowLength = 2.5f
//        )
//        // After `laneVisualParams` is configured you need to set it to `VisionArView`
//        visionArView.setLaneVisualParams(laneVisualParams)
//
//        // Enable fence rendering
//        visionArView.setFenceVisible(true)
//
//        val fenceVisualParams = FenceVisualParams(
//            // Set yellow fence color
//            color = Color(1.0f, 1.0f, 0.0f, 1.0f),
//            // Set the height of the fence in meters
//            height = 2f,
//            // Set the vertical offset of the fence above the road surface in meters
//            verticalOffset = 1f,
//            // Set the horizontal offset of the fence from the center of the road in meters
//            horizontalOffset = 3f,
//            // Set the number of arrows in the fence
//            sectionsCount = 3
//        )
//        // After `fenceVisualParams` is configured you need to set it to to `VisionArView`
//        visionArView.setFenceVisualParams(fenceVisualParams)
//        // Setting AR render overall quality a bit lower to gain performance
//        visionArView.setArQuality(0.8f)
//    }
//}
