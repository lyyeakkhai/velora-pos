package com.velora.app.modules.store_managementModule.domain;

import com.velora.app.core.utils.ValidationUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Embedded (flattened) address value object.
 */
public final class Address {

    private static final Map<String, String> CAMBODIAN_PROVINCES;

    static {
        Map<String, String> provinces = new HashMap<>();
        addProvince(provinces, "Banteay Meanchey");
        addProvince(provinces, "Battambang");
        addProvince(provinces, "Kampong Cham");
        addProvince(provinces, "Kampong Chhnang");
        addProvince(provinces, "Kampong Speu");
        addProvince(provinces, "Kampong Thom");
        addProvince(provinces, "Kampot");
        addProvince(provinces, "Kandal");
        addProvince(provinces, "Kep");
        addProvince(provinces, "Koh Kong");
        addProvince(provinces, "Kratie");
        addProvince(provinces, "Mondulkiri");
        addProvince(provinces, "Oddar Meanchey");
        addProvince(provinces, "Pailin");
        addProvince(provinces, "Phnom Penh");
        addProvince(provinces, "Preah Sihanouk");
        addProvince(provinces, "Preah Vihear");
        addProvince(provinces, "Pursat");
        addProvince(provinces, "Prey Veng");
        addProvince(provinces, "Ratanakiri");
        addProvince(provinces, "Siem Reap");
        addProvince(provinces, "Stung Treng");
        addProvince(provinces, "Svay Rieng");
        addProvince(provinces, "Takeo");
        addProvince(provinces, "Tboung Khmum");
        CAMBODIAN_PROVINCES = Collections.unmodifiableMap(provinces);
    }

    private final String street;
    private final String city;
    private final String district;
    private final String province;

    public Address(String street, String city, String district, String province) {
        ValidationUtils.validateNotBlank(street, "street");
        ValidationUtils.validateNotBlank(city, "city");
        ValidationUtils.validateNotBlank(district, "district");
        ValidationUtils.validateNotBlank(province, "province");

        this.street = street.trim();
        this.city = city.trim();
        this.district = district.trim();
        this.province = normalizeCambodianProvince(province);
    }

    private static void addProvince(Map<String, String> provinces, String canonical) {
        provinces.put(canonical.toLowerCase(), canonical);
    }

    public static String normalizeCambodianProvince(String province) {
        ValidationUtils.validateNotBlank(province, "province");
        String canonical = CAMBODIAN_PROVINCES.get(province.trim().toLowerCase());
        if (canonical == null) {
            throw new IllegalArgumentException("province must be an official Cambodian province");
        }
        return canonical;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getProvince() {
        return province;
    }
}
