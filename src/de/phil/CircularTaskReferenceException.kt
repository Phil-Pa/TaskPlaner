package de.phil

class CircularTaskReferenceException(message: String = "There is no task without dependencies. No task can be done this way.") : Exception(message) {

}
