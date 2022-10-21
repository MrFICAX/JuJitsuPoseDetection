package elfak.diplomski.jujitsuposedetection.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class resultsViewModel: ViewModel() {
    fun addClassName(className: String) {
        _repCounter.value?.put(className, 0);
    }

    fun addRepToClassName(className: String){
        (_repCounter.value?.get(className)?.plus(1) ?: _repCounter.value?.get(className))?.let {
            _repCounter.value?.put(className,
                it
            )
        }
    }

    //
    private val _repCounter: MutableLiveData<HashMap<String, Int>> = MutableLiveData(HashMap<String, Int>())
    val repCounter: MutableLiveData<HashMap<String, Int>>
        get() = _repCounter


}