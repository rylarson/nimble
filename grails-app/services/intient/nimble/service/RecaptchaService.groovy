package intient.nimble.serviceimport net.tanesha.recaptcha.ReCaptchaFactoryimport net.tanesha.recaptcha.*import grails.util.GrailsUtilimport org.springframework.beans.factory.InitializingBeanclass RecaptchaService implements InitializingBean {  boolean transactional = false  public static String SESSION_KEY = "intient.nimble.service.recaptcha"  public static String SESSION_ERROR_KEY = "intient.nimble.service.recaptcha.error"  def publicKey  def privateKey  def includeNoScript  def secureAPI  boolean enabled  def grailsApplication  void afterPropertiesSet() {    publicKey = grailsApplication.config.nimble.recaptcha.publickey    privateKey = grailsApplication.config.nimble.recaptcha.privatekey    includeNoScript = grailsApplication.config.nimble.recaptcha.noscript    enabled = grailsApplication.config.nimble.recaptcha.enabled    secureAPI = grailsApplication.config.nimble.recaptcha.secureapi  }    /**   * Creates HTML containing all necessary markup for displaying a ReCapture object   *   * @param session The current session. Used for short term storage of the recaptcha object and any error messages.   * @param props Properties used to construct the HTML. See http://recaptcha.net/apidocs/captcha/client.html for valid   * properties.   *   * @return HTML code, suitable for embedding into a webpage.   */  def createCaptcha(session, props) {    ReCaptcha recap    log.debug("Creating ReCaptcha markup")    if(!enabled) {      log.debug("ReCaptcha disabled, outputting blank markup")      return ""    }    if (secureAPI) {      recap = ReCaptchaFactory.newSecureReCaptcha(publicKey, privateKey, includeNoScript)    } else {      recap = ReCaptchaFactory.newReCaptcha(publicKey, privateKey, includeNoScript)    }    session[RecaptchaService.SESSION_KEY] = recap    log.debug("ReCaptcha enabled, outputting correct markup")    return recap.createRecaptchaHtml(session[RecaptchaService.SESSION_ERROR_KEY], props)  }  /**   * Verify a ReCaptcha answer.   *   * @param session The current session.   * @param remoteAddress The address of the browser submitting the answer.   * @param params Parameters supplied by the browser.   *   * @return True if the supplied answer is correct, false otherwise. Returns true if ReCaptcha support is disabled.   */  def verifyAnswer(session, remoteAddress, params) {    if (!isEnabled()) {      return true    }    ReCaptcha recaptcha = session[RecaptchaService.SESSION_KEY]    if (recaptcha) {      def response = recaptcha.checkAnswer(remoteAddress, params.recaptcha_challenge_field?.trim(), params.recaptcha_response_field?.trim())      if (!response.valid) {        session[RecaptchaService.SESSION_ERROR_KEY] = response.errorMessage      }      cleanUp(session)      return response.valid    }    return false  }  /**   * Determine if the previous verification attempt failed.   *   * @param session The current session   */  def validationFailed(session) {    return (session[RecaptchaService.SESSION_ERROR_KEY] != null)  }  /**   * Cleanup recaptcha resources associated with the session.   *   * @param session The current session.   */  def cleanUp(session) {    session[RecaptchaService.SESSION_KEY] = null    session[RecaptchaService.SESSION_ERROR_KEY] = null  }}