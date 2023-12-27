"use client"

import axios from "axios";
import {useEffect, useState} from "react"

type Response = {
ping: String,
}

const TestComponent = () => {
    const [test, setTest] = useState<String>("");

    useEffect(() => {
        async function query() {
            const res = await axios.get("/api")

            const response = res.data as Response
            setTest(response.ping)
        }

        query()
    }, [])

    return (
        <div>
            Test Component
            <div>
                Server Response: {test}
            </div>
        </div>
    )
}

export default TestComponent
