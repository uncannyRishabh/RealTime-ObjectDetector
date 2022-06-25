# RealTime-ObjectDetector
RTOD is an app to demonstrate the way to achieve object detection and classification in <i>Realtime</i> for android.

We know that ML Kit api can track object(s) in realtime with ease, even in lower end devices. But the default classifier that comes with ML Kit can only classify objects into 5 categories. We can use a custom classifier e.g. [MobileNet V3](https://tfhub.dev/google/lite-model/imagenet/mobilenet_v3_small_100_224/classification/5/metadata/1) which is trained on ImageNet (ILSVRC-2012-CLS) dataset and can classify objects into 1001 categories. While 1001 sounds a lot, it is not that much, also the accuracy drops severely, specially in real-world scenarios. Whats the point in realtime object detection if its not accurate.
While playing around with android profiler, I found out that there the app was only consuming about 10-20% of CPU usage, while running realtime detection and classification on my Redmi note 9 pro (sd720G), which led to converting this one stage approach into two stage. ML Kit API can classify objects into 5 categories by default(fashion good, home good, plants, place, food). For each object classified in an image frame there is a tracking ID assigned to it, which helps to track object in successive image frames. Once an object is detected by the detector, we draw the bounding box over it, and pass the cropped object to an External classifier which will produce a label. 

External classifiers are trained on object specific dataset e.g (Food, plant, fashion, home good, places), and will be invoked only if the ML Kit api classifies an object. By using an External classifier, we can narrow down the classification and the accuracy improves hugely. 

On the design front we followed [Material design principles](https://material.io/design/machine-learning/understanding-ml-patterns.html) to incorporate best practices. 

## Future Scope
- [ ] Product search.
- [ ] Read text from camera(TTS). 
- [ ] Live text recognition and translation.

## Limitation
- Delay between detection and Classification (Bounding box and Label).
- Apk size increases, since we are bundling multiple tflite models.

## App Link
[DOWNLOAD](https://github.com/uncannyRishabh/RealTime-ObjectDetector/blob/master/app/release/app-release.apk)
