package `in`.procyk.bring.ai

import ai.koog.prompt.markdown.markdown
import ai.koog.prompt.params.LLMParams
import ai.koog.prompt.structure.StructuredData
import ai.koog.prompt.structure.json.generator.BasicJsonSchemaGenerator
import ai.koog.prompt.structure.json.generator.JsonSchemaGenerator
import ai.koog.prompt.structure.json.generator.StandardJsonSchemaGenerator
import ai.koog.prompt.structure.structure
import ai.koog.prompt.text.TextContentBuilderBase
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

internal class MarkdownWrappedJsonStructuredData<TStruct> private constructor(
    id: String,
    schema: LLMParams.Schema.JSON,
    examples: List<TStruct>,
    val serializer: KSerializer<TStruct>,
    val json: Json,
    private val definitionPrompt: (
        builder: TextContentBuilderBase<*>,
        structuredData: MarkdownWrappedJsonStructuredData<TStruct>,
    ) -> TextContentBuilderBase<*> = ::defaultDefinitionPrompt,
) : StructuredData<TStruct, LLMParams.Schema.JSON>(id, schema, examples) {

    override fun parse(text: String): TStruct =
        json.decodeFromString(serializer, text.trim().removeSurrounding("```json", "```"))

    override fun pretty(value: TStruct): String =
        json.encodeToString(serializer, value)

    override fun definition(builder: TextContentBuilderBase<*>): TextContentBuilderBase<*> =
        definitionPrompt(builder, this)

    companion object {
        val defaultJson: Json = Json {
            prettyPrint = true
            explicitNulls = false
            isLenient = true
            ignoreUnknownKeys = true
            classDiscriminator = "#type"
            classDiscriminatorMode = ClassDiscriminatorMode.POLYMORPHIC
        }

        fun <TStruct> defaultDefinitionPrompt(
            builder: TextContentBuilderBase<*>,
            structuredData: MarkdownWrappedJsonStructuredData<TStruct>,
        ): TextContentBuilderBase<*> = builder.apply {
            with(structuredData) {
                markdown {
                    h3("DEFINITION OF $id")

                    +"The $id format is defined only and solely with JSON, without any additional characters, comments, backticks or anything similar."
                    br()

                    +"You must adhere to the following JSON schema:"
                    +json.encodeToString(schema.schema)
                    br()

                    if (examples.isNotEmpty()) {
                        h4("EXAMPLES")

                        if (examples.size == 1) {
                            +"Here is an example of a valid response:"
                        } else {
                            +"Here are some examples of valid responses:"
                        }

                        examples.forEach { example ->
                            codeblock(
                                code = ai.koog.prompt.text.text { structure(this@with, example) },
                                language = "json"
                            )
                        }
                    }

                    h2("RESULT")
                    +"Provide ONLY the resulting JSON, WITHOUT ANY free text comments, backticks, or other symbols."
                    +"Output should start with { and end with }"

                    newline()
                }
            }
        }

        fun <TStruct> createMarkdownWrappedJsonStructure(
            id: String,
            serializer: KSerializer<TStruct>,
            json: Json = defaultJson,
            schemaGenerator: JsonSchemaGenerator = StandardJsonSchemaGenerator.Default,
            descriptionOverrides: Map<String, String> = emptyMap(),
            examples: List<TStruct> = emptyList(),
            definitionPrompt: (
                builder: TextContentBuilderBase<*>,
                structuredData: MarkdownWrappedJsonStructuredData<TStruct>,
            ) -> TextContentBuilderBase<*> = ::defaultDefinitionPrompt,
        ): MarkdownWrappedJsonStructuredData<TStruct> {
            return MarkdownWrappedJsonStructuredData(
                id = id,
                schema = schemaGenerator.generate(json, id, serializer, descriptionOverrides),
                examples = examples,
                serializer = serializer,
                json = json,
                definitionPrompt = definitionPrompt,
            )
        }

        inline fun <reified TStruct> createMarkdownWrappedJsonStructure(
            json: Json = defaultJson,
            schemaGenerator: JsonSchemaGenerator = BasicJsonSchemaGenerator.Default,
            descriptionOverrides: Map<String, String> = emptyMap(),
            examples: List<TStruct> = emptyList(),
            noinline definitionPrompt: (
                builder: TextContentBuilderBase<*>,
                structuredData: MarkdownWrappedJsonStructuredData<TStruct>,
            ) -> TextContentBuilderBase<*> = ::defaultDefinitionPrompt,
        ): MarkdownWrappedJsonStructuredData<TStruct> {
            val serializer = serializer<TStruct>()

            return createMarkdownWrappedJsonStructure(
                id = serializer.descriptor.serialName.substringAfterLast("."),
                serializer = serializer,
                json = json,
                schemaGenerator = schemaGenerator,
                descriptionOverrides = descriptionOverrides,
                examples = examples,
                definitionPrompt = definitionPrompt,
            )
        }
    }
}
