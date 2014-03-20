package grails.plugins.imports

class ImportsException extends Exception {
  public int responseCode = 500
  public String importLogId

  public ImportsException() {
    super('Request could not be completed')
  } 

  public ImportsException(String msg) {
    super(msg?.toString())
  } 

  public ImportsException(int code) {
    super()
    this.responseCode = code
  } 


  public ImportsException(String msg, int code) {
    super(msg?.toString())
    this.responseCode = code
  } 
  
  public ImportsException(String msg, Throwable cause ) {
    super(msg?.toString(), cause)
  } 

  public ImportsException(int code, Throwable cause) {
    super(cause)
    this.responseCode = code
  } 


  public ImportsException(String msg, int code, Throwable cause) {
    super(msg?.toString(), cause)
    this.responseCode = code
    this.importLogId = importLogId_
  } 

  public ImportsException(String msg, String importLogId_) {
    super(msg?.toString())
    this.importLogId = importLogId_
  } 

  public ImportsException(int code, String importLogId_) {
    super()
    this.responseCode = code
    this.importLogId = importLogId_
  } 


  public ImportsException(String msg, int code, String importLogId_) {
    super(msg?.toString())
    this.responseCode = code
    this.importLogId = importLogId_
  } 
  
  public ImportsException(String msg, String importLogId_, Throwable cause ) {
    super(msg?.toString(), cause)
    this.importLogId = importLogId_
  } 

  public ImportsException(int code, String importLogId_, Throwable cause) {
    super(cause)
    this.responseCode = code
    this.importLogId = importLogId_
  } 


  public ImportsException(String msg, int code, String importLogId_, Throwable cause) {
    super(msg?.toString(), cause)
    this.responseCode = code
    this.importLogId = importLogId_
  } 

}