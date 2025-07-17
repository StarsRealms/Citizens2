rootProject.name = "Citizens2"
include(":citizens-main")
include(":citizens")
include(":citizens-v1_21_R5")
project(":citizens-main").projectDir = file("main")
project(":citizens").projectDir = file("dist")
project(":citizens-v1_21_R5").projectDir = file("v1_21_R5")