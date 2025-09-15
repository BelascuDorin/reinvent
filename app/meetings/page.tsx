"use client"

import { useSession } from "next-auth/react"
import { useRouter } from "next/navigation"
import { useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { trpc } from "@/lib/trpc"
import { Users, ArrowLeft, Calendar, Clock, Euro, Plus } from "lucide-react"
import Link from "next/link"

export default function MeetingsPage() {
  const { data: session, status } = useSession()
  const router = useRouter()

  useEffect(() => {
    if (status === "loading") return
    if (!session) {
      router.push("/auth/signin")
      return
    }
  }, [session, status, router])

  const { data: bookings, isLoading } = trpc.meeting.getMyBookings.useQuery()
  const { data: mentorSlots } = trpc.meeting.getMentorSlots.useQuery(undefined, {
    enabled: session?.user.role === "mentor",
  })

  const updateStatusMutation = trpc.meeting.updateBookingStatus.useMutation({
    onSuccess: () => {
      // Refetch bookings
      window.location.reload()
    },
  })

  if (status === "loading" || isLoading) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div>Loading...</div>
      </div>
    )
  }

  if (!session) {
    return null
  }

  const isMentor = session.user.role === "mentor"

  const getStatusColor = (status: string) => {
    switch (status) {
      case "pending":
        return "bg-yellow-100 text-yellow-800"
      case "confirmed":
        return "bg-green-100 text-green-800"
      case "completed":
        return "bg-blue-100 text-blue-800"
      case "cancelled":
        return "bg-red-100 text-red-800"
      default:
        return "bg-gray-100 text-gray-800"
    }
  }

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="border-b border-border bg-card">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center space-x-4">
            <Link href="/dashboard">
              <Button variant="ghost" size="sm">
                <ArrowLeft className="w-4 h-4 mr-2" />
                Back to Dashboard
              </Button>
            </Link>
            <div className="flex items-center space-x-2">
              <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center">
                <Users className="w-5 h-5 text-primary-foreground" />
              </div>
              <h1 className="text-xl font-semibold text-foreground">{isMentor ? "My Sessions" : "My Meetings"}</h1>
            </div>
          </div>
          {isMentor && (
            <Link href="/meetings/create">
              <Button>
                <Plus className="w-4 h-4 mr-2" />
                Create Session
              </Button>
            </Link>
          )}
        </div>
      </header>

      <div className="container mx-auto px-4 py-8">
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-foreground mb-2">
            {isMentor ? "Manage Your Sessions" : "Your Booked Meetings"}
          </h2>
          <p className="text-muted-foreground">
            {isMentor
              ? "View and manage your mentoring sessions and availability"
              : "Track your upcoming meetings with mentors"}
          </p>
        </div>

        {/* Mentor: Available Slots */}
        {isMentor && mentorSlots && (
          <div className="mb-8">
            <h3 className="text-xl font-semibold text-foreground mb-4">Available Time Slots</h3>
            {mentorSlots.filter((slot) => !slot.isBooked).length === 0 ? (
              <Card>
                <CardContent className="py-8 text-center">
                  <Calendar className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                  <p className="text-muted-foreground mb-4">No available slots created yet</p>
                  <Link href="/meetings/create">
                    <Button>Create Your First Session</Button>
                  </Link>
                </CardContent>
              </Card>
            ) : (
              <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
                {mentorSlots
                  .filter((slot) => !slot.isBooked)
                  .map((slot) => (
                    <Card key={slot.id}>
                      <CardHeader>
                        <CardTitle className="text-lg">{slot.title}</CardTitle>
                        <CardDescription>{slot.description}</CardDescription>
                      </CardHeader>
                      <CardContent>
                        <div className="space-y-2">
                          <div className="flex items-center text-sm text-muted-foreground">
                            <Calendar className="w-4 h-4 mr-2" />
                            {new Date(slot.date).toLocaleDateString()}
                          </div>
                          <div className="flex items-center text-sm text-muted-foreground">
                            <Clock className="w-4 h-4 mr-2" />
                            {slot.startTime} - {slot.endTime}
                          </div>
                          <div className="flex items-center text-sm text-muted-foreground">
                            <Euro className="w-4 h-4 mr-2" />
                            {slot.price === 0 ? "Free" : `${slot.price} RON`}
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
              </div>
            )}
          </div>
        )}

        {/* Bookings */}
        <div>
          <h3 className="text-xl font-semibold text-foreground mb-4">
            {isMentor ? "Booked Sessions" : "Your Meetings"}
          </h3>
          {!bookings || bookings.length === 0 ? (
            <Card>
              <CardContent className="py-8 text-center">
                <Calendar className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                <p className="text-muted-foreground mb-4">
                  {isMentor ? "No sessions booked yet" : "No meetings booked yet"}
                </p>
                {!isMentor && (
                  <Link href="/browse">
                    <Button>Browse Mentors</Button>
                  </Link>
                )}
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-4">
              {bookings.map((booking) => (
                <Card key={booking.id}>
                  <CardHeader>
                    <div className="flex items-start justify-between">
                      <div>
                        <CardTitle className="text-lg">{booking.title}</CardTitle>
                        <CardDescription>
                          {isMentor ? `with ${booking.menteeName}` : `with ${booking.mentorName}`}
                        </CardDescription>
                      </div>
                      <Badge className={getStatusColor(booking.status)}>{booking.status}</Badge>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <div className="grid md:grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <div className="flex items-center text-sm text-muted-foreground">
                          <Calendar className="w-4 h-4 mr-2" />
                          {new Date(booking.date).toLocaleDateString()}
                        </div>
                        <div className="flex items-center text-sm text-muted-foreground">
                          <Clock className="w-4 h-4 mr-2" />
                          {booking.startTime} - {booking.endTime}
                        </div>
                        <div className="flex items-center text-sm text-muted-foreground">
                          <Euro className="w-4 h-4 mr-2" />
                          {booking.price === 0 ? "Free" : `${booking.price} RON`}
                        </div>
                      </div>

                      {booking.message && (
                        <div>
                          <p className="text-sm font-medium text-foreground mb-1">Message:</p>
                          <p className="text-sm text-muted-foreground">{booking.message}</p>
                        </div>
                      )}
                    </div>

                    {/* Action buttons for mentors */}
                    {isMentor && booking.status === "pending" && (
                      <div className="flex gap-2 mt-4">
                        <Button
                          size="sm"
                          onClick={() =>
                            updateStatusMutation.mutate({
                              bookingId: booking.id,
                              status: "confirmed",
                            })
                          }
                        >
                          Confirm
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() =>
                            updateStatusMutation.mutate({
                              bookingId: booking.id,
                              status: "cancelled",
                            })
                          }
                        >
                          Cancel
                        </Button>
                      </div>
                    )}

                    {/* Mark as completed */}
                    {booking.status === "confirmed" && (
                      <div className="mt-4">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() =>
                            updateStatusMutation.mutate({
                              bookingId: booking.id,
                              status: "completed",
                            })
                          }
                        >
                          Mark as Completed
                        </Button>
                      </div>
                    )}
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
