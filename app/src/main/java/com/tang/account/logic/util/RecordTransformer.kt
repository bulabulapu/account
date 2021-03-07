package com.tang.account.logic.util

import com.tang.account.R

/*Record数据转换器*/
object RecordTransformer {
    const val XIAN_JIN = "xian_jin"
    const val ZHI_FU_BAO = "zhi_fu_bao"
    const val WEI_XIN = "wei_xin"
    const val YIN_HANG_KA = "yin_hang_ka"
    const val XIN_YONG_KA = "xin_yong_ka"
    const val QI_TA = "qi_ta"
    const val XIAO_FEI = "xiao_fei"
    const val ZHUAN_ZHANG = "zhuan_zhang"
    const val CAN_YIN = "can_yin"
    const val JIAO_TONG = "jiao_tong"
    const val YU_LE = "yu_le"
    const val GOU_WU = "gou_wu"
    const val TONG_XUN = "tong_xun"
    const val AA = "aa"
    const val HONG_BAO = "hong_bao"
    const val QU_KUAN = "qu_kuan"
    const val SHENG_HUO = "sheng_huo"
    const val ZU_FANG = "zu_fang"
    const val YI_LIAO = "yi_liao"
    const val JIAO_YU = "jiao_yu"
    const val SHOU_RU = "shou_ru"

    /*类别列表*/
    val categoryList = listOf(
        XIAO_FEI,
        ZHUAN_ZHANG,
        CAN_YIN,
        JIAO_TONG,
        YU_LE,
        GOU_WU,
        TONG_XUN,
        AA,
        HONG_BAO,
        QU_KUAN,
        SHENG_HUO,
        ZU_FANG,
        YI_LIAO,
        JIAO_YU,
        SHOU_RU,
        QI_TA
    )

    /*支出类别列表*/
    val categoryExpenseList = listOf(
        XIAO_FEI,
        ZHUAN_ZHANG,
        CAN_YIN,
        JIAO_TONG,
        YU_LE,
        GOU_WU,
        TONG_XUN,
        AA,
        HONG_BAO,
        QU_KUAN,
        SHENG_HUO,
        ZU_FANG,
        YI_LIAO,
        JIAO_YU,
        QI_TA
    )

    /*收入类别列表*/
    val categoryIncomeList = listOf(
        SHOU_RU
    )

    /*way和category转换*/
    private val transformList = mapOf(
        XIAN_JIN to "现金",
        ZHI_FU_BAO to "支付宝",
        WEI_XIN to "微信",
        YIN_HANG_KA to "银行卡",
        XIN_YONG_KA to "信用卡",
        QI_TA to "其他",
        XIAO_FEI to "消费",
        ZHUAN_ZHANG to "转帐",
        CAN_YIN to "餐饮",
        JIAO_TONG to "交通",
        YU_LE to "娱乐",
        GOU_WU to "购物",
        TONG_XUN to "通讯",
        AA to "AA",
        HONG_BAO to "红包",
        QU_KUAN to "取款",
        SHENG_HUO to "生活",
        ZU_FANG to "租房",
        YI_LIAO to "医疗",
        JIAO_YU to "教育",
        SHOU_RU to "收入",
        "现金" to XIAN_JIN,
        "支付宝" to ZHI_FU_BAO,
        "微信" to WEI_XIN,
        "银行卡" to YIN_HANG_KA,
        "信用卡" to XIN_YONG_KA,
        "其他" to QI_TA,
        "消费" to XIAO_FEI,
        "转帐" to ZHUAN_ZHANG,
        "餐饮" to CAN_YIN,
        "交通" to JIAO_TONG,
        "娱乐" to YU_LE,
        "购物" to GOU_WU,
        "通讯" to TONG_XUN,
        "AA" to AA,
        "红包" to HONG_BAO,
        "取款" to QU_KUAN,
        "生活" to SHENG_HUO,
        "租房" to ZU_FANG,
        "医疗" to YI_LIAO,
        "教育" to JIAO_YU,
        "收入" to SHOU_RU
    )

    /*category图标列表*/
    private val imageResList = mapOf(
        QI_TA to R.drawable.category_other,
        XIAO_FEI to R.drawable.category_spending,
        ZHUAN_ZHANG to R.drawable.category_remittance,
        CAN_YIN to R.drawable.category_catering,
        JIAO_TONG to R.drawable.category_traffic,
        YU_LE to R.drawable.category_entertainment,
        GOU_WU to R.drawable.category_shopping,
        TONG_XUN to R.drawable.category_telephone_charge,
        AA to R.drawable.category_go_dutch,
        HONG_BAO to R.drawable.category_red_packet,
        QU_KUAN to R.drawable.category_atm,
        SHENG_HUO to R.drawable.category_life,
        ZU_FANG to R.drawable.category_tenement,
        YI_LIAO to R.drawable.category_medical,
        JIAO_YU to R.drawable.category_education,
        SHOU_RU to R.drawable.category_income
    )

    /*category颜色*/
    private val categoryColorList = mapOf(
        QI_TA to R.color.categoryOtherColor,
        XIAO_FEI to R.color.categorySpendingColor,
        ZHUAN_ZHANG to R.color.categoryRemittanceColor,
        CAN_YIN to R.color.categoryCateringColor,
        JIAO_TONG to R.color.categoryTrafficColor,
        YU_LE to R.color.categoryEntertainmentColor,
        GOU_WU to R.color.categoryShoppingColor,
        TONG_XUN to R.color.categoryTelephoneChargeColor,
        AA to R.color.categoryGoDutchColor,
        HONG_BAO to R.color.categoryRedPacketColor,
        QU_KUAN to R.color.categoryATMColor,
        SHENG_HUO to R.color.categoryLifeColor,
        ZU_FANG to R.color.categoryTenementColor,
        YI_LIAO to R.color.categoryMedicalColor,
        JIAO_YU to R.color.categoryEducationColor,
        SHOU_RU to R.color.categoryIncomeColor
    )

    /* MIUI数据库文件中category的id列表*/
    private val categoryMIUIId = mapOf(
        "1" to XIAO_FEI,
        "2" to ZHUAN_ZHANG,
        "3" to CAN_YIN,
        "4" to JIAO_TONG,
        "5" to YU_LE,
        "6" to GOU_WU,
        "7" to TONG_XUN,
        "8" to AA,
        "9" to HONG_BAO,
        "10" to QU_KUAN,
        "11" to SHENG_HUO,
        "12" to ZU_FANG,
        "13" to YI_LIAO,
        "14" to JIAO_YU,
        "15" to QI_TA
    )

    /* MIUI数据库文件中way的id列表*/
    private val wayMIUIId = mapOf(
        "1" to XIAN_JIN,
        "10" to QI_TA,
        "100" to YIN_HANG_KA,
        "101" to XIN_YONG_KA,
        "200" to ZHI_FU_BAO,
        "300" to WEI_XIN
    )

    /**
     * 根据key返回相对应的value
     * @param key 传入的key
     * @return 相应的value或者error_type
     */
    fun transform(key: String) = transformList[key.trim()] ?: "error_type"

    /**
     * 根据传入的category的key值返回图片id
     * @param key category的key
     * @return 图片id
     */
    fun getImageId(key: String) = imageResList[key.trim()] ?: R.drawable.category_other

    /**
     * 根据传入的category的key值返回对应类别的颜色
     * @param key category的key
     * @return 颜色值id
     */
    fun getCategoryColor(key: String) = categoryColorList[key.trim()] ?: R.color.categoryOtherColor

    /**
     * 根据miui数据category的id返回category
     * @param id miui id
     * @return category的拼音
     */
    fun getCategoryByMIUIId(id: String) = categoryMIUIId[id.trim()] ?: QI_TA

    /**
     * 根据miui数据way的id返回way
     * @param id miui id
     * @return category的拼音
     */
    fun getWayByMIUIId(id: String) = wayMIUIId[id.trim()] ?: QI_TA
}