# TWRP
TW_THEME := portrait_hdpi

# Keymaster
TARGET_KEYMASTER_WAIT_FOR_QSEE := true

#Camera Hacks
TARGET_HAS_LEGACY_CAMERA_HAL1 := true
BOARD_GLOBAL_CFLAGS += -DMETADATA_CAMERA_SOURCE

