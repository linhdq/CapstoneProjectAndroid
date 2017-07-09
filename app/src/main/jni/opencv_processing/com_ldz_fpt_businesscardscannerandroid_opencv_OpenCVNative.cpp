#include "com_ldz_fpt_businesscardscannerandroid_opencv_OpenCVNative.h"

int img_x,img_y;
Mat ori_image;
Mat imv_copy;
std::vector<std::vector<cv::Point> > contours;
std::vector<Vec4i> hierarchy;

JNIEXPORT void JNICALL Java_com_ldz_fpt_businesscardscannerandroid_opencv_OpenCVNative_convertGray
        (JNIEnv *, jclass, jlong addrRgba, jlong addrGray){
    ori_image = *(Mat *) addrRgba;
    Mat &dest = *(Mat *) addrGray;

    imv_copy = ori_image.clone();
    img_y = imv_copy.size().height;
    img_x = imv_copy.size().width;
    Mat gray;
    cvtColor(imv_copy, gray, CV_BGR2GRAY);
//    Mat blur_image;
//    GaussianBlur(gray, blur_image, cv::Size(3,3), 0);

    float sigma = 0.33;
    float v = median_mat(gray);
    int lower_val =int((1.0 - sigma) * v);
    int upper_val =int((1.0 + sigma) * v);
    int lower = max(0, lower_val);
    int upper = min(255, upper_val);

    Canny(gray, gray, lower, upper);

    findContours( gray, contours, hierarchy,
            CV_RETR_TREE, CV_CHAIN_APPROX_NONE );

    std::vector<std::vector<cv::Point> > keeper_contour;
    std::vector<cv::Rect> keeper_rect;

    for(int i=0;i<contours.size();i++){
        cv::Rect rect = boundingRect(contours[i]);

        if (keep(contours[i]) && include_box(i, contours[i])) {
            keeper_contour.push_back(contours[i]);
            keeper_rect.push_back(rect);
        }
    }

    //create new image
    imv_copy.setTo(Scalar::all(255));

    std::vector<cv::Point> contour;

    float fg_int;
    float bg_int;
    int x_, y_, width, height;
    int fg, bg;
    double bg_array[12];
    //For each box, find the foreground and background intensities
    for (int i=0; i<keeper_contour.size(); i++) {
        fg_int = 0.0;
        bg_int = 0.0;
        //foreground intensity
        contour = keeper_contour[i];
        for (int j=0;j<contour.size(); j++) {
            fg_int+=pix_intensity(contour[j].x, contour[j].y);
        }
        fg_int/=contour.size();
        //background intensity
        x_=keeper_rect[i].x;
        y_=keeper_rect[i].y;
        width=keeper_rect[i].size().width;
        height=keeper_rect[i].size().height;

        bg_array[0] = pix_intensity(x_ - 1, y_ - 1);
        bg_array[1] = pix_intensity(x_ - 1, y_);
        bg_array[2] = pix_intensity(x_, y_ - 1);
        bg_array[3] = pix_intensity(x_ + width + 1, y_ - 1);
        bg_array[4] = pix_intensity(x_ + width, y_ - 1);
        bg_array[5] = pix_intensity(x_ + width + 1, y_);
        bg_array[6] = pix_intensity(x_ - 1, y_ + height + 1);
        bg_array[7] = pix_intensity(x_ - 1, y_ + height);
        bg_array[8] = pix_intensity(x_, y_ + height + 1);
        bg_array[9] = pix_intensity(x_ + width + 1, y_ + height + 1);
        bg_array[10] = pix_intensity(x_ + width, y_ + height + 1);
        bg_array[11] = pix_intensity(x_ + width + 1, y_ + height);

        bg_int = cal_median(bg_array, 12);

        if (fg_int>=bg_int) {
            fg = 255;
            bg = 0;
        }else{
            bg = 255;
            fg = 0;
        }

        for (int x_index=x_; x_index< x_+width ; x_index++) {
            for (int y_index=y_; y_index<y_+height; y_index++) {
                if (y_index >= img_y || x_index >= img_x) {
                    continue;
                }
                Vec4b color = imv_copy.at<Vec4b>(y_index,x_index);
                if (pix_intensity(x_index, y_index)>fg_int) {
                    color[0] = bg;
                    color[1] = bg;
                    color[2] = bg;
                    color[3] = bg;
                }else{
                    color[0] = fg;
                    color[1] = fg;
                    color[2] = fg;
                    color[3] = fg;
                }
                imv_copy.at<Vec4b>(y_index,x_index) = color;

            }
        }

    }
    blur(imv_copy, imv_copy, cv::Size(2,2));
    dest = imv_copy;
}

bool keep_box(std::vector<cv::Point> contour){
    cv::Rect rect  = boundingRect(contour);
    float w = rect.width*1.0;
    float h = rect.height*1.0;

    if(w/h <0.1 || w/h>10.0){
        return false;
    }

    if (w*h > img_x*img_y*1.0/5 || w*h < 15) {
        return false;
    }

    return true;
}

bool connected(std::vector<cv::Point> contour){
    cv::Point firt = contour[0];
    cv::Point last = contour[contour.size()-1];
    return (abs(firt.x - last.x) <=1) && (abs(firt.y -last.y)<=1);
}

float pix_intensity(int x, int y){
    if (x>=img_x || y>=img_y) {
        return 0;
    }
    Vec4b pix = ori_image.at<Vec4b>(y,x);
    return 0.30 * pix[2] + 0.59 * pix[1] + 0.11 * pix[0];
}

std::vector<cv::Point> get_contour(int index){
    return contours[index];
}

int get_parent(int index){
    return hierarchy[index][3];
}

bool is_child(int index){
    return get_parent(index)>0;
}

bool keep(std::vector<cv::Point> contour){
    return keep_box(contour) && connected(contour);
}

int count_childs(int index, std::vector<cv::Point> contour){
    //no child
    int child_index = hierarchy[index][2];
    int count =0;
    if (child_index<0) {
        return 0;
    }
    if (keep(get_contour(child_index))) {
        count+=1;
    }
    //
    int previous = hierarchy[child_index][1];
    while (previous>0) {
        if (keep(get_contour(previous))) {
            count+=1;
        }
        previous=hierarchy[previous][1];
    }

    int next = hierarchy[child_index][0];
    while (next>0) {
        if (keep(get_contour(next))) {
            count+=1;
        }
        next=hierarchy[next][0];
    }
    return count;
}

bool include_box(int index, std::vector<cv::Point> contour){
    if (is_child(index) && count_childs(get_parent(index), get_contour(get_parent(index)))<=2) {
        return false;
    }
    if (count_childs(index, contour)>2) {
        return false;
    }
    return true;
}

double cal_median(double daArray[], int iSize) {
    // Allocate an array of the same size and sort it.
    double* dpSorted = new double[iSize];
    for (int i = 0; i < iSize; ++i) {
        dpSorted[i] = daArray[i];
    }
    for (int i = iSize - 1; i > 0; --i) {
        for (int j = 0; j < i; ++j) {
            if (dpSorted[j] > dpSorted[j+1]) {
                double dTemp = dpSorted[j];
                dpSorted[j] = dpSorted[j+1];
                dpSorted[j+1] = dTemp;
            }
        }
    }

    // Middle or average of middle values in the sorted array.
    double dMedian = 0.0;
    if ((iSize % 2) == 0) {
        dMedian = (dpSorted[iSize/2] + dpSorted[(iSize/2) - 1])/2.0;
    } else {
        dMedian = dpSorted[iSize/2];
    }
    delete [] dpSorted;
    return dMedian;
}

double median_mat(Mat channel )
{
    double m = (channel.rows*channel.cols) / 2;
    int bin = 0;
    double med = -1.0;

    int histSize = 256;
    float range[] = { 0, 256 };
    const float* histRange = { range };
    bool uniform = true;
    bool accumulate = false;
    cv::Mat hist;
    cv::calcHist( &channel, 1, 0, cv::Mat(), hist, 1, &histSize, &histRange, uniform, accumulate );

    for ( int i = 0; i < histSize && med < 0.0; ++i )
    {
        bin += cvRound( hist.at< float >( i ) );
        if ( bin > m && med < 0.0 )
            med = i;
    }
    return med;
}

JNIEXPORT jfloatArray JNICALL Java_com_ldz_fpt_businesscardscannerandroid_opencv_OpenCVNative_detectCardVisit
        (JNIEnv *env, jclass , jlong addrRgba) {
    env->FindClass("com/ldz/fpt/businesscardscannerandroid/opencv/OpenCVNative");
    cv::Mat &original = *(Mat *) addrRgba;
    vector <vector<Point> > squares;
    vector <Point> largest_square;
    jfloatArray jfloatArray1;
    jfloat listPoint[8];

    find_squares(original, squares);
    find_largest_square(squares, largest_square);
    if (largest_square.size() == 4) {
        jfloatArray1 = env->NewFloatArray(8);
        Point temp, tl, tr, bl, br;
        for (int i = 0; i < largest_square.size() - 1; i++) {
            for (int j = i + 1; j < largest_square.size(); j++) {
                if (largest_square[i].y > largest_square[j].y) {
                    temp = largest_square[i];
                    largest_square[i] = largest_square[j];
                    largest_square[j] = temp;
                }
            }
        }
        if(largest_square[0].x<largest_square[1].x){
            tl = largest_square[0];
            tr = largest_square[1];
        }else{
            tl = largest_square[1];
            tr = largest_square[0];
        }
        if(largest_square[2].x<largest_square[3].x){
            bl = largest_square[2];
            br = largest_square[3];
        }else{
            bl = largest_square[3];
            br = largest_square[2];
        }
        int width = original.size().width;
        int height = original.size().height;
        if(bl.y - tl.y <= height/5 || br.y - tr.y <= height/5
           || tr.x-tl.x <= width/5 || br.x-bl.x <= width/5){
            listPoint[0] = 0;
            listPoint[1] = 0;
            listPoint[2] = width;
            listPoint[3] = 0;
            listPoint[4] = width;
            listPoint[5] = height;
            listPoint[6] = 0;
            listPoint[7] = height;
        }else{
            listPoint[0] = tl.x;
            listPoint[1] = tl.y;
            listPoint[2] = tr.x;
            listPoint[3] = tr.y;
            listPoint[4] = br.x;
            listPoint[5] = br.y;
            listPoint[6] = bl.x;
            listPoint[7] = bl.y;
        }
    }else{
        return NULL;
    }
    env->SetFloatArrayRegion(jfloatArray1, 0, 8, listPoint);
    return jfloatArray1;
}

void find_squares(cv::Mat& image, std::vector<std::vector<cv::Point> >&squares) {
    // blur will enhance edge detection
    Mat blurred;
    GaussianBlur(image.clone(), blurred, cvSize(9,9), 0);//change from median blur to gaussian for more accuracy of square detection

    Mat gray0(blurred.size(), CV_8U), gray;
    std::vector<std::vector<cv::Point> > contours;

    // find squares in every color plane of the image
    for (int c = 0; c < 3; c++) {
        int ch[] = {c, 0};
        mixChannels(&blurred, 1, &gray0, 1, ch, 1);

        // try several threshold levels
        const int threshold_level = 2;
        for (int l = 0; l < threshold_level; l++) {
            //             Use Canny instead of zero threshold level!
            //             Canny helps to catch squares with gradient shading
            if (l == 0) {
                Canny(gray0, gray, 10, 20, 3);

                // Dilate helps to remove potential holes between edge segments
                dilate(gray, gray, cv::Mat(), cv::Point(-1,-1));
            }

            // Find contours and store them in a list
            findContours(gray, contours, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);

            // Test contours
            std::vector<cv::Point> approx;
            for (size_t i = 0; i < contours.size(); i++) {
                // approximate contour with accuracy proportional
                // to the contour perimeter
                approxPolyDP(cv::Mat(contours[i]), approx, arcLength(cv::Mat(contours[i]), true)*0.02, true);

                // Note: absolute value of an area is used because
                // area may be positive or negative - in accordance with the
                // contour orientation
                if (approx.size() == 4 &&
                    fabs(contourArea(cv::Mat(approx))) > 1000 &&
                    isContourConvex(cv::Mat(approx))) {
                    double maxCosine = 0;

                    for (int j = 2; j < 5; j++) {
                        double cosine = fabs(angle(approx[j%4], approx[j-2], approx[j-1]));
                        maxCosine = MAX(maxCosine, cosine);
                    }

                    if (maxCosine < 0.3) {
                        squares.push_back(approx);
                    }
                }
            }
        }
    }
}

void find_largest_square(const std::vector<std::vector<cv::Point> >& squares, std::vector<Point>& biggest_square) {
    if (!squares.size()) {
        // no squares detected
        return;
    }
    int max_square_idx = 0;
    int area = 0;

    for (int i = 0; i < squares.size(); i++) {
        if(contourArea(squares[i])>area){
            max_square_idx=i;
        }
    }
    biggest_square = squares[max_square_idx];
}

double angle( cv::Point pt1, cv::Point pt2, cv::Point pt0 ) {
    double dx1 = pt1.x - pt0.x;
    double dy1 = pt1.y - pt0.y;
    double dx2 = pt2.x - pt0.x;
    double dy2 = pt2.y - pt0.y;
    return (dx1*dx2 + dy1*dy2)/sqrt((dx1*dx1 + dy1*dy1)*(dx2*dx2 + dy2*dy2) + 1e-10);
}

JNIEXPORT void JNICALL Java_com_ldz_fpt_businesscardscannerandroid_opencv_OpenCVNative_increaseContrast
        (JNIEnv *, jclass, jlong addrRgba, jlong addrRgbaContrast){
    Mat& ori_image = *(Mat*) addrRgba;
    Mat& destImage = *(Mat*) addrRgbaContrast;
    Mat newImage = Mat::zeros(ori_image.size(), ori_image.type());
    ori_image.convertTo(newImage,-1, 1.9, -90);
    destImage = newImage;
}

struct byArea {
    bool operator () (const Rect & a,const Rect & b) {
        return a.width*a.height > b.width*b.height ;
    }
};

std::vector<cv::Rect> detectLetters(cv::Mat img){
    std::vector<cv::Rect> boundRect;
    cv::Mat img_gray, img_sobel, img_threshold, element;
    cvtColor(img, img_gray, CV_BGR2GRAY);
    cv::Sobel(img_gray, img_sobel, CV_8U, 1, 0, 3, 1, 0, cv::BORDER_DEFAULT);
    cv::threshold(img_sobel, img_threshold, 0, 255, CV_THRESH_OTSU+CV_THRESH_BINARY);
    element = getStructuringElement(cv::MORPH_RECT, cv::Size(30, 30) );
    cv::morphologyEx(img_threshold, img_threshold, CV_MOP_CLOSE, element); //Does the trick
    std::vector< std::vector< cv::Point> > contours;
    cv::findContours(img_threshold, contours, 0, 1);
    std::vector<std::vector<cv::Point> > contours_poly( contours.size() );
    for( int i = 0; i < contours.size(); i++ ) {
        if (contours[i].size()>100) {
            cv::approxPolyDP( cv::Mat(contours[i]), contours_poly[i], 3, true );
            cv::Rect appRect(boundingRect( cv::Mat(contours_poly[i]) ));
            if(appRect.x >=15){
                appRect.x -= 15;
            }else{
                appRect.x = 0;
            }
            if(appRect.y >=10){
                appRect.y -= 10;
            }else{
                appRect.y = 0;
            }
            if(appRect.x + appRect.width <= img.size().width - 30){
                appRect.width += 30;
            }else{
                appRect.width = img.size().width - appRect.x;
            }
            if(appRect.y + appRect.height <= img.size().height - 20){
                appRect.height += 20;
            }else{
                appRect.height = img.size().height - appRect.y;
            }
            if (appRect.width>appRect.height&& appRect.width>=30 && appRect.height>=20) {
                boundRect.push_back(appRect);
            }
        }
    }
    sort(boundRect.begin(), boundRect.end(), byArea());
    Rect rect1, rect2;
    int index1 = 0;
    int index2=0;
    int count=0;
    while(index1<boundRect.size()-1){
        index2 = index1+1;
        rect1 = boundRect[index1];
        count=0;
        while (index2<boundRect.size()) {
            rect2 = boundRect[index2];
            //new rect inside another rect
            if (rect2.y>=rect1.y && rect2.y+rect2.height<= rect1.height+rect1.y
                && rect2.x>=rect1.x && rect2.x+rect2.width<=rect1.width+rect1.x) {
                boundRect.erase(boundRect.begin()+index2);
                count++;
                break;
            }else if (rect1.y>=rect2.y && rect1.height+rect1.y<= rect2.y+rect2.height
                      && rect1.x>=rect2.x && rect1.x+rect1.width<=rect2.width+rect2.x) { //current rect is inside new rect
                boundRect.erase(boundRect.begin()+index1);
                count++;
                break;
            }else if(rect2.y+rect2.height>=rect1.y && rect2.y<=rect1.y+rect1.height
                     && rect2.x+rect2.width>= rect1.x-15 && rect2.x<= rect1.x+ rect1.width+15){//check x&y axist
                boundRect[index1].width= max(rect1.x+rect1.width, rect2.x+rect2.width) - min(rect2.x, rect1.x);
                boundRect[index1].height = max(rect1.y+rect1.height, rect2.y+rect2.height) - min(rect2.y, rect1.y);
                boundRect[index1].x = min(rect1.x, rect2.x);
                boundRect[index1].y = min(rect1.y, rect2.y);
                boundRect.erase(boundRect.begin()+index2);
                count++;
                break;
            }
            index2++;
        }
        if(count==0){
            index1++;
        }
    }
    return boundRect;
}


JNIEXPORT jintArray JNICALL Java_com_ldz_fpt_businesscardscannerandroid_opencv_OpenCVNative_detectTextBlock
        (JNIEnv *env, jclass, jlong addrRgba){
    env->FindClass("com/ldz/fpt/businesscardscannerandroid/opencv/OpenCVNative");
    Mat& ori_image = *(Mat*) addrRgba;
    //Detect
    std::vector<cv::Rect> rects =detectLetters(ori_image);
    jintArray jintArray1;
    jint listPoint[rects.size()*4];
    jintArray1 = env->NewIntArray(rects.size()*4);
    int index =0;
    //Display
    for(int i=0; i< rects.size(); i++) {
        listPoint[index++] = rects[i].x;
        listPoint[index++] = rects[i].y;
        listPoint[index++] = rects[i].width;
        listPoint[index++] = rects[i].height;
    }
    env->SetIntArrayRegion(jintArray1, 0, rects.size()*4, listPoint);
    return jintArray1;
}

JNIEXPORT void JNICALL Java_com_ldz_fpt_businesscardscannerandroid_opencv_OpenCVNative_cropMat
        (JNIEnv *, jclass, jlong matS, jlong matD, jint x, jint y, jint w, jint h){
    Mat &mat_s = *(Mat*) matS;
    Mat &mat_d = *(Mat*) matD;
    Rect rect(x,y,w,h);
    mat_d = mat_s(rect);
}