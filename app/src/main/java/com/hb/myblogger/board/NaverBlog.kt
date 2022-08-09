package com.hb.myblogger.board

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import org.json.JSONArray


/**
 * Blog Scheme를 사용하기 위한 예시 class
 * version=1에서는 write만 존재합니다.
 * 차후 다양한 Scheme가 추가될 예정입니다.
 */
class NaverBlog(private val context: Context) {
    /**
     * 첨부 글쓰기 스키마
     * @param version 버전
     * @param title 제목
     * @param content 내용
     * @param imageUrls 이미지url
     * @param videoUrls 동영상url
     * @param ogTagUrls 오지링크url
     * @param tags 태그
     */
    fun write(
        version: Int,
        title: String?,
        content: String?,
        imageUrls: List<String?>?,
        videoUrls: List<String?>?,
        ogTagUrls: List<String?>?,
        tags: List<String?>?
    ) {
        val writeUri =
            BlogUriBuilder.write(version, title, content, imageUrls, videoUrls, ogTagUrls, tags)
        val writeIntent = Intent()
        writeIntent.data = writeUri
        try {
            context.startActivity(writeIntent)
        } catch (e: ActivityNotFoundException) {
            gotoMarket()
        }
    }

    /**
     * 마켓으로 이동
     */
    fun gotoMarket() {
        // 네이버 앱스토어로 갈 수 없으면, 플레이 스토어로 이동
        if (!gotoNaverAppStore()) {
            gotoPlayStore()
        }
    }

    /**
     * 네이버 앱스토어 이동
     */
    fun gotoNaverAppStore(): Boolean {
        if (isAppInstalled(APP_NAVER_APPSTORE)) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(BLOG_INSTALL_URL_NAVER)
            context.startActivity(intent)
            return true
        }
        return false
    }

    /**
     * 구글 플레이스토어 이동
     */
    fun gotoPlayStore() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(BLOG_INSTALL_URL)
        context.startActivity(intent)
    }

    /**
     * 앱설치 확인
     */
    fun isAppInstalled(packname: String?): Boolean {
        try {
            val info = context.packageManager.getPackageInfo(
                packname!!, 0
            )
            if (info != null) {
                return true
            }
        } catch (e: PackageManager.NameNotFoundException) {
        }
        return false
    }

    /**
     * write Url Scheme 생성
     * (List를 받아 jsonArray로 파싱하는 방식을 사용하였지만 처음부터 jsonArray로 만들어서 보내는 방법도 있습니다.)
     */
    object BlogUriBuilder {
        fun write(
            version: Int,
            title: String?,
            content: String?,
            imageUrls: List<String?>?,
            videoUrls: List<String?>?,
            ogTagUrls: List<String?>?,
            tags: List<String?>?
        ): Uri {
            val uriBuilder = Uri.Builder()
            uriBuilder.scheme(SCHEME_NAVERBLOG)
            uriBuilder.authority(AUTHORITY_WRITE)
            uriBuilder.appendQueryParameter(QUERY_VERSION, version.toString())
            if (title != null && !title.isEmpty()) {
                uriBuilder.appendQueryParameter(QUERY_TITLE, title)
            }
            if (content != null && !content.isEmpty()) {
                uriBuilder.appendQueryParameter(QUERY_CONTENT, content)
            }
            appendArrayQueryParameter(uriBuilder, QUERY_IMAGEURLS, imageUrls)
            appendArrayQueryParameter(uriBuilder, QUERY_VIDEOURLS, videoUrls)
            appendArrayQueryParameter(uriBuilder, QUERY_OGTAGURLS, ogTagUrls)
            appendArrayQueryParameter(uriBuilder, QUERY_TAGS, tags)
            return uriBuilder.build()
        }

        fun appendArrayQueryParameter(
            uriBuilder: Uri.Builder,
            queryString: String?,
            list: List<String?>?
        ) {
            if (list == null) {
                return
            }
            val jsArray = JSONArray(list)
            uriBuilder.appendQueryParameter(queryString, jsArray.toString())
        }
    }

    companion object {
        const val APP_NAVER_APPSTORE = "com.nhn.android.appstore"
        const val BLOG_INSTALL_URL = "market://details?id=com.nhn.android.blog"
        const val BLOG_INSTALL_URL_NAVER =
            "appstore://store?version=7&action=view&packageName=com.nhn.android.blog"
        const val SCHEME_NAVERBLOG = "naverblog"
        const val AUTHORITY_WRITE = "write"
        const val QUERY_VERSION = "version"
        const val QUERY_TITLE = "title"
        const val QUERY_CONTENT = "content"
        const val QUERY_IMAGEURLS = "imageUrls"
        const val QUERY_VIDEOURLS = "videoUrls"
        const val QUERY_OGTAGURLS = "ogTagUrls"
        const val QUERY_TAGS = "tags"
    }
}
