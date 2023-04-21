package com.example.a3d_printer_1.model
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class PrintFileViewModel : ViewModel() {
    private var _file_name = MutableLiveData<String>("")
    var file_name: LiveData<String> = _file_name

    private var _file_num_lines = MutableLiveData<String>("")
    var file_num_lines: LiveData<String> = _file_num_lines

    private var _button_text = MutableLiveData<String>("Start Print")
    var button_text: LiveData<String> = _button_text

    private var _has_file = MutableLiveData<Boolean>(false)
    var has_file: LiveData<Boolean> = _has_file

    private var _has_started_print = MutableLiveData<Boolean>(false)
    var has_started_print: LiveData<Boolean> = _has_started_print

    private var _home_is_busy = MutableLiveData<Boolean>(false)
    var home_is_busy: LiveData<Boolean> = _home_is_busy

    fun setFileName(desiredName: String) {
//        _file_name.value = desiredName
        _file_name.value = desiredName
    }

    fun setButtonText(desiredBtnText: String) {
        _button_text.value = desiredBtnText
    }

    fun setFileNumLines(desiredNumLines: String) {
        _file_num_lines.value = desiredNumLines
    }

    fun setHasFile(hasFile: Boolean) {
        _has_file.value = hasFile
    }

    fun setHasStartedPrint(hasStartedPrint: Boolean) {
        _has_started_print.value = hasStartedPrint
    }

    fun setHomeIsBusy(homeIsBusy: Boolean) {
        _home_is_busy.value = homeIsBusy
    }

    fun readFileName(): String {
        return _file_name.value!!
    }

    fun readFileNumLines(): String {
        return _file_num_lines.value!!
    }

    fun readButtonText(): String {
        return _button_text.value!!
    }

    fun readHasFile(): Boolean {
        return _has_file.value!!
    }

    fun readHasStartedPrint(): Boolean {
        return _has_started_print.value!!
    }

    fun readHomeIsBusy(): Boolean {
        return _home_is_busy.value!!
    }
}

////DELETE=========================================================================================
//private val _quantity = MutableLiveData<Int>(0)
//val quantity: LiveData<Int> = _quantity
//
//private val _flavor = MutableLiveData<String>("")
//val flavor: LiveData<String> = _flavor
//fun setFlavor(desiredFlavor: String) {
//    _flavor.value = desiredFlavor
//}
//
//private val _date = MutableLiveData<String>("")
//val date: LiveData<String> = _date
//private val _price = MutableLiveData<Double>(0.0)
//val price: LiveData<Double> = _price
//
//fun setQuantity(numberCupcakes: Int) {
//    _quantity.value = numberCupcakes
//}
//fun setFlavor(desiredFlavor: String) {
//    _flavor.value = desiredFlavor
//}
//fun setDate(pickupDate: String) {
//    _date.value = pickupDate
//
//}fun hasNoFlavorSet(): Boolean {
//    return _flavor.value.isNullOrEmpty()
//}