package com.example.a3d_printer_1

class gcodeFileClass {
    var name: String? = null
    var url: String? = null
    var date: String? = null
    var size: String? = null

    constructor() {}
    constructor(name: String?, url: String?, date: String?, size: String?) {
        this.name = name
        this.url = url
        this.date = date
        this.size = size
    }
}