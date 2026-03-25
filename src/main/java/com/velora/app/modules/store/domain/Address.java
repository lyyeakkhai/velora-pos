package com.velora.app.modules.store.domain;

import java.util.Objects;

/**
 * Value object representing a physical address in Cambodia.
 * 
 * <p>
 * Address is immutable and validates province against official Cambodian
 * provinces.
 * The class follows the value object pattern with equals/hashCode based on
 * field values.
 */
public final class Address {

    /**
     * Official Cambodian provinces for validation.
     */
    public enum CambodianProvince {
        PHNOM_PENH("Phnom Penh"),
        BAT_DAMBANG("Banteay Meanchey"),
        BANTEAY_MEANCHEY("Banteay Meanchey"),
        KAMPONG_CHAM("Kampong Cham"),
        KAMPONG_CHHNANG("Kampong Chhnang"),
        KAMPONG_SPEU("Kampong Speu"),
        KAMPONG_THOM("Kampong Thom"),
        KAMPOT("Kampot"),
        KANDAL("Kandal"),
        KOH_KONG("Koh Kong"),
        KRATIE("Kratie"),
        MONDUL_KIRI("Mondul Kiri"),
        PHNOM_PENH_CITY("Phnom Penh"),
        PREAH_VIHEAR("Preah Vihear"),
        PREY_VENG("Prey Veng"),
        PURSAT("Pursat"),
        RATTANAKIRI("Rattanakiri"),
        SIEM_REAP("Siem Reap"),
        PREAH_SIHANOUK("Preah Sihanouk"),
        STUNG_TRENG("Stung Treng"),
        SVAY_RIENG("Svay Rieng"),
        TAKEO("Takeo"),
        ODDAR_MEANCHEY("Oddar Meanchey"),
        BATTAMBANG("Battambang"),
        KEPP("Kep"),
        PAILIN("Pailin");

        private final String displayName;

        CambodianProvince(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final String street;
    private final String city;
    private final String district;
    private final CambodianProvince province;
    private final String postalCode;

    /**
     * Creates a new Address.
     *
     * @param street     Street address line (required)
     * @param city       City name (required)
     * @param district   District name (optional, may be null)
     * @param province   Cambodian province (required)
     * @param postalCode Postal/ZIP code (optional, may be null)
     */
    public Address(String street, String city, String district,
            CambodianProvince province, String postalCode) {
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("Street cannot be null or blank");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City cannot be null or blank");
        }
        if (province == null) {
            throw new IllegalArgumentException("Province cannot be null");
        }

        this.street = street.trim();
        this.city = city.trim();
        this.district = district != null ? district.trim() : null;
        this.province = province;
        this.postalCode = postalCode != null ? postalCode.trim() : null;
    }

    /**
     * Creates an Address with only required fields.
     *
     * @param street   Street address line
     * @param city     City name
     * @param province Cambodian province
     */
    public Address(String street, String city, CambodianProvince province) {
        this(street, city, null, province, null);
    }

    /**
     * Gets the street address line.
     */
    public String getStreet() {
        return street;
    }

    /**
     * Gets the city name.
     */
    public String getCity() {
        return city;
    }

    /**
     * Gets the district name.
     */
    public String getDistrict() {
        return district;
    }

    /**
     * Gets the province.
     */
    public CambodianProvince getProvince() {
        return province;
    }

    /**
     * Gets the postal code.
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * Returns the full address as a formatted string.
     */
    public String getFormatted() {
        StringBuilder sb = new StringBuilder();
        sb.append(street);
        if (district != null) {
            sb.append(", ").append(district);
        }
        sb.append(", ").append(city);
        sb.append(", ").append(province.getDisplayName());
        if (postalCode != null) {
            sb.append(" ").append(postalCode);
        }
        return sb.toString();
    }

    /**
     * Returns the address as a single-line string.
     */
    public String toSingleLine() {
        return getFormatted();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Address address = (Address) o;
        return Objects.equals(street, address.street) &&
                Objects.equals(city, address.city) &&
                Objects.equals(district, address.district) &&
                province == address.province &&
                Objects.equals(postalCode, address.postalCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, city, district, province, postalCode);
    }

    @Override
    public String toString() {
        return "Address{" +
                "street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", district='" + district + '\'' +
                ", province=" + province +
                ", postalCode='" + postalCode + '\'' +
                '}';
    }
}
