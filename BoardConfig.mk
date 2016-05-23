#
# Copyright (C) 2015 The AOSParadox Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

PLATFORM_PATH := device/oneplus/bacon

# Include path
TARGET_SPECIFIC_HEADER_PATH := $(PLATFORM_PATH)/include

# Bootloader
TARGET_BOOTLOADER_BOARD_NAME := MSM8974
TARGET_NO_BOOTLOADER := true
TARGET_NO_RADIOIMAGE := true

# Platform
TARGET_BOARD_PLATFORM := msm8974
TARGET_BOARD_PLATFORM_GPU := qcom-adreno330
QCOM_BOARD_PLATFORMS += msm8974

# Architecture
TARGET_ARCH := arm
TARGET_ARCH_VARIANT := armv7-a-neon
TARGET_CPU_ABI := armeabi-v7a
TARGET_CPU_ABI2 := armeabi
TARGET_CPU_VARIANT := krait
 
# Asserts
TARGET_BOARD_INFO_FILE ?= device/oneplus/bacon/board-info.txt
TARGET_OTA_ASSERT_DEVICE := bacon,A0001

# Kernel
TARGET_KERNEL_CONFIG := bacon_defconfig
KERNEL_DEFCONFIG := bacon_defconfig
BOARD_KERNEL_CMDLINE := console=ttyHSL0,115200,n8 androidboot.hardware=bacon user_debug=31 msm_rtb.filter=0x3F ehci-hcd.park=3 androidboot.bootdevice=msm_sdcc.1 zcache.enabled=1 zcache.compressor=lz4
BOARD_KERNEL_TAGS_OFFSET := 0x01e00000
BOARD_RAMDISK_OFFSET     := 0x02000000
BOARD_KERNEL_BASE        := 0x00000000
BOARD_KERNEL_PAGESIZE    := 2048
TARGET_KERNEL_CROSS_COMPILE_PREFIX := arm-linux-androideabi-
TARGET_KERNEL_SOURCE := kernel/oneplus/msm8974
BOARD_KERNEL_SEPARATED_DT := true
BOARD_DTBTOOL_ARGS := -2
TARGET_KERNEL_ARCH := arm

# ANT+
BOARD_ANT_WIRELESS_DEVICE := "vfs-prerelease"

# Init
TARGET_INIT_VENDOR_LIB := libinit_msm
TARGET_LIBINIT_DEFINES_FILE := device/oneplus/bacon/init/init_bacon.cpp

# RIL
COMMON_GLOBAL_CFLAGS += -DNO_SECURE_DISCARD
COMMON_GLOBAL_CPPFLAGS += -DNO_SECURE_DISCARD

# QCOM hardware
BOARD_USES_QCOM_HARDWARE := true

# Audio
BOARD_USES_ALSA_AUDIO := true
AUDIO_FEATURE_ENABLED_HWDEP_CAL := true
AUDIO_FEATURE_ENABLED_LOW_LATENCY_CAPTURE := true
AUDIO_FEATURE_ENABLED_MULTI_VOICE_SESSIONS := true
AUDIO_FEATURE_ENABLED_NEW_SAMPLE_RATE := true
AUDIO_FEATURE_LOW_LATENCY_PRIMARY := true
AUDIO_FEATURE_ENABLED_COMPRESS_VOIP := false
USE_CUSTOM_AUDIO_POLICY := 1

# Bluetooth
BOARD_BLUETOOTH_BDROID_BUILDCFG_INCLUDE_DIR := device/oneplus/bacon/bluetooth
BOARD_HAVE_BLUETOOTH_QCOM := true
QCOM_BT_USE_SMD_TTY := true
BLUETOOTH_HCI_USE_MCT := true
FEATURE_QCRIL_UIM_SAP_SERVER_MODE := true

# Camera
TARGET_USE_COMPAT_GRALLOC_ALIGN := true
USE_DEVICE_SPECIFIC_CAMERA := true
COMMON_GLOBAL_CFLAGS += -DOPPO_CAMERA_HARDWARE -DCAMERA_VENDOR_L_COMPAT

# Crypto
TARGET_HW_DISK_ENCRYPTION := true
# Tap to wake
TARGET_TAP_TO_WAKE_NODE := "/proc/touchpanel/double_tap_enable"

# Graphics
TARGET_CONTINUOUS_SPLASH_ENABLED := true
MAX_EGL_CACHE_KEY_SIZE := 12*1024
MAX_EGL_CACHE_SIZE := 2048*1024
OVERRIDE_RS_DRIVER := libRSDriver_adreno.so
TARGET_USES_ION := true
USE_OPENGL_RENDERER := true
TARGET_USES_C2D_COMPOSITION := true
NUM_FRAMEBUFFER_SURFACE_BUFFERS := 3

# Filesystem
BOARD_FLASH_BLOCK_SIZE := 131072
BOARD_BOOTIMAGE_PARTITION_SIZE     := 16777216
BOARD_CACHEIMAGE_FILE_SYSTEM_TYPE := ext4
BOARD_CACHEIMAGE_PARTITION_SIZE    := 536870912
BOARD_PERSISTIMAGE_FILE_SYSTEM_TYPE := ext4
BOARD_PERSISTIMAGE_PARTITION_SIZE  := 33554432
BOARD_RECOVERYIMAGE_PARTITION_SIZE := 16777216
BOARD_SYSTEMIMAGE_PARTITION_SIZE   := 1388314624
BOARD_USERDATAIMAGE_FILE_SYSTEM_TYPE := ext4
BOARD_USERDATAIMAGE_PARTITION_SIZE := 13271448576
BOARD_USERDATAEXTRAIMAGE_PARTITION_SIZE := 59914792960
BOARD_USERDATAEXTRAIMAGE_PARTITION_NAME := 64G
TARGET_USERIMAGES_USE_EXT4 := true

# GPS
USE_DEVICE_SPECIFIC_LOC_API := true
USE_DEVICE_SPECIFIC_GPS := true

# Keymaster
TARGET_KEYMASTER_WAIT_FOR_QSEE := true

# Lights
TARGET_PROVIDES_LIBLIGHT := true

# NFC
BOARD_NFC_CHIPSET := pn547

# Wifi
BOARD_HAS_QCOM_WLAN              := true
BOARD_HAS_QCOM_WLAN_SDK          := true
BOARD_WLAN_DEVICE                := qcwcn
BOARD_WPA_SUPPLICANT_DRIVER      := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_qcwcn
BOARD_HOSTAPD_DRIVER             := NL80211
BOARD_HOSTAPD_PRIVATE_LIB        := lib_driver_cmd_qcwcn
TARGET_USES_WCNSS_CTRL           := true
TARGET_USES_QCOM_WCNSS_QMI       := true
TARGET_USES_WCNSS_MAC_ADDR_REV   := true
TARGET_WCNSS_MAC_PREFIX          := e8bba8
WIFI_DRIVER_FW_PATH_STA          := "sta"
WIFI_DRIVER_FW_PATH_AP           := "ap"
WPA_SUPPLICANT_VERSION           := VER_0_8_X

# Recovery
TARGET_RECOVERY_FSTAB := device/oneplus/bacon/rootdir/etc/fstab.bacon

# Enable Minikin text layout engine (will be the default soon)
USE_MINIKIN := true

TARGET_KERNEL_HAVE_NTFS := true

# RPC
TARGET_NO_RPC := true

#WITH_DEXPREOPT := true

# Sepolicy
include device/qcom/sepolicy/sepolicy.mk

# QCNE
BOARD_USES_QCNE := true

ifeq ($(BOARD_USES_QCNE),true)
TARGET_LDPRELOAD := libNimsWrap.so
endif

BOARD_SEPOLICY_DIRS += \
        device/oneplus/bacon/sepolicy
#skip boot jars check if QCPATH not available
SKIP_BOOT_JARS_CHECK := true
