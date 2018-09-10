package notification.email

import java.util

import com.fasterxml.jackson.annotation.JsonProperty
import com.sendgrid.Personalization

/**
  * Workaround for SendGrid API V3 bug...
  */

class PersonalizationWrapper extends Personalization {

  @JsonProperty("dynamic_template_data")
  val dynamicTemplateData: util.HashMap[String, Object] = new util.HashMap


  @JsonProperty("dynamic_template_data")
  def getDynamicTemplateData() = dynamicTemplateData


  def addDynamicTemplateData(key: String, value: Object): Unit = {
    dynamicTemplateData.put(key, value)
  }





}
