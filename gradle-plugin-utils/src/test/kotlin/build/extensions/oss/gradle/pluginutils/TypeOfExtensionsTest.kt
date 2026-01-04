package build.extensions.oss.gradle.pluginutils

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.gradle.api.reflect.TypeOf


@Suppress("UnstableApiUsage")
class TypeOfExtensionsTest : DescribeSpec({

    describe("typeOf") {

        it("should create a TypeOf for a simple type") {

            val type = typeOf<SimpleType>()
            val expectedType = TypeOf.typeOf(SimpleType::class.java)

            type shouldBe expectedType
        }


        it("should create a TypeOf for an array type") {

            val type = typeOf<Array<SimpleType>>()
            val expectedType = TypeOf.typeOf(Array<SimpleType>::class.java)

            type shouldBe expectedType
        }


        it("should create a TypeOf for a parameterized type") {

            val type = typeOf<Pair<String, SimpleType>>()

            val expectedType = object : TypeOf<Pair<String, SimpleType>>() {}

            type shouldBe expectedType
        }
    }
})


class SimpleType
