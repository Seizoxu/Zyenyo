"use client"

import { StarIcon } from "@heroicons/react/24/outline";
import { TrophyIcon } from "@heroicons/react/24/outline";
import { CommandLineIcon } from "@heroicons/react/24/outline";

<CommandLineIcon className="h-6 w-6 text-gray-500" />

import Link from "next/link";

<StarIcon className="h-6 w-6 text-gray-500" />

import {useEffect, useState} from "react"

const Dashboard = () => {

    return (
        <>
            <div className='flex flex-row gap-0 text-6xl md:text-9xl'>
                <div className='text-white underline underline-offset-8'>Zyenyo</div><div className='text-[#F51A1F]'>Bot</div>
            </div>
            <div className="flex flex-col mt-auto mb-auto gap-3">

                <a className="relative hover:bg-zinc-800 ease-in p-6 gap-3 min-w-[250px] text-center border-2 border-white rounded-3xl text-lg font-bold" href="https://discord.com/api/oauth2/authorize?client_id=696614233944752130&permissions=137439283200&scope=bot" target="_blank">

                    <StarIcon className="absolute left-5 top-auto bottom-auto h-8 -translate-y-[2px] w-8 text-[#F51A1F]" />
                    Get Zyenyo
                </a>

                <Link className="relative hover:bg-zinc-800 ease-in p-6 gap-3  min-w-[250px] text-center border-2 border-white rounded-3xl text-lg font-bold" href="/">

                    <CommandLineIcon className="absolute left-5 top-auto bottom-auto -translate-y-[2px]  h-8 w-8 text-[#F51A1F]" />
                    Commands
                </Link>

                <Link className="relative hover:bg-zinc-800 ease-in p-6 gap-3  min-w-[250px] text-center border-2 border-white rounded-3xl text-lg font-bold" href="/">

                    <TrophyIcon className="absolute left-5 top-auto bottom-auto -translate-y-[2px]  h-8 w-8 text-[#F51A1F]" />
                    Leaderboards
                </Link>

            </div>
        </>

    )
}

export default Dashboard
