package controller

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import models.DataStorage
import models.FeeResponse

@Controller("/fees")
class FeeCollectedController {
    @Get("/")
    fun getFees(): HttpResponse<FeeResponse> {
        println(DataStorage.TOTAL_FEE_COLLECTED)
        return HttpResponse.ok(FeeResponse(DataStorage.TOTAL_FEE_COLLECTED))
    }

}