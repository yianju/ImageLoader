package com.eaju.imageloader.imageLoader

import android.graphics.Point
/**
 * Description:
 * Copyright  : Copyright (c) 2016
 * Company    : Android
 * Author     : 关羽
 * Date       : 2018-07-31 14:34
 */
class YAJLoadOption {

    //默认图片
    var mDefaultImg: Int = 0

    //错误图片
    var mErrorImg: Int = 0

    //是否圆形
    var isCircle: Boolean = false

    //是否播放gif
    var isPlayGif: Boolean = true

    //大小
    var mSize: Point? = null

    //图片
    var mUri: Any? = null

    val mTransformations: ArrayList<Any> = ArrayList()

    fun setDefaultImg(defaultImg: Int): YAJLoadOption {
        this.mDefaultImg = defaultImg
        return this
    }

    fun setErrorImg(errorImg: Int): YAJLoadOption {
        this.mErrorImg = errorImg
        return this
    }

    /**
     * 是否圆形，目前支持fresco 、 glide
     */
    fun setCircle(circle: Boolean): YAJLoadOption {
        this.isCircle = circle
        return this
    }

    /**
     * 是否播放gif，只支持Fresco目前
     */
    fun setPlayGif(playGif: Boolean): YAJLoadOption {
        this.isPlayGif = playGif
        return this
    }

    /**
     * 目标尺寸
     */
    fun setSize(size: Point?): YAJLoadOption {
        this.mSize = size
        return this
    }

    /**
     * 播放目标 string、uri、int
     */
    fun setUri(uri: Any): YAJLoadOption {
        this.mUri = uri
        return this
    }

    /**
     * 图片处理
     * picasso https://github.com/wasabeef/picasso-transformations
     * glide   https://github.com/wasabeef/glide-transformations
     * fresco  https://github.com/wasabeef/fresco-processors
     */
    fun setTransformations(transform: Any): YAJLoadOption {
        mTransformations.add(transform)
        return this
    }
}
