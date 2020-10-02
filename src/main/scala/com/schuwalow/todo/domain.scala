package com.schuwalow.todo

import io.circe.Decoder
import io.circe.generic.semiauto._


final case class Report(id: Long, body: String) {

  def update(form: TodoItemPatchForm): Report =
    this.copy(id = this.id, body = form.body.getOrElse(""))
}

final case class TodoItemPostForm(title: String, order: Option[Int] = None) {

  def asTodoItem(id: Long): Report = Report(id, "")

}

object TodoItemPostForm {
  implicit val decoder: Decoder[TodoItemPostForm] = deriveDecoder
}

final case class TodoItemPatchForm(body: Option[String] = None)

object TodoItemPatchForm {
  implicit val decoder: Decoder[TodoItemPatchForm] = deriveDecoder
}
