import XCTest
import Capacitor
@testable import Plugin

class PluginTests: XCTestCase {
    var plugin: CameraPreview?

    override func setUp() {
        super.setUp()
        plugin = CameraPreview()
    }

    override func tearDown() {
        plugin = nil
        super.tearDown()
    }

    func testStartCamera() {
        let call = CAPPluginCall(callbackId: "testStartCamera", options: [
            "position": "rear",
            "quality": 85,
            "width": 800,
            "height": 600
        ], success: { (_, _) in
            // Camera started successfully
            XCTAssert(true)
        }, error: { (_) in
            XCTFail("Error shouldn't have been called")
        })

        plugin?.start(call!)
    }

    func testFlipCamera() {
        let call = CAPPluginCall(callbackId: "testFlipCamera", options: [:], success: { (_, _) in
            // Camera flipped successfully
            XCTAssert(true)
        }, error: { (_) in
            XCTFail("Error shouldn't have been called")
        })

        plugin?.flip(call!)
    }

    func testStopCamera() {
        let call = CAPPluginCall(callbackId: "testStopCamera", options: [:], success: { (_, _) in
            // Camera stopped successfully
            XCTAssert(true)
        }, error: { (_) in
            XCTFail("Error shouldn't have been called")
        })

        plugin?.stop(call!)
    }

    func testStartRecordVideo() {
        let call = CAPPluginCall(callbackId: "testStartRecordVideo", options: [:], success: { (_, _) in
            // Video recording started successfully
            XCTAssert(true)
        }, error: { (_) in
            XCTFail("Error shouldn't have been called")
        })

        plugin?.startRecordVideo(call!)
    }

    func testStopRecordVideo() {
        let expectation = self.expectation(description: "Video recording stopped")

        let call = CAPPluginCall(callbackId: "testStopRecordVideo", options: [:], success: { (result, _) in
            // Video recording stopped successfully
            let videoUrl = result?.data?["videoUrl"] as? String
            XCTAssertNotNil(videoUrl)
            expectation.fulfill()
        }, error: { (_) in
            XCTFail("Error shouldn't have been called")
        })

        plugin?.stopRecordVideo(call!)

        waitForExpectations(timeout: 5, handler: nil)
    }
}
